package ru.kochkaev.zixamc.tgbridge.telegram

import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.config.TextData
import ru.kochkaev.zixamc.tgbridge.telegram.model.*
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult.Companion.DELETE_LINKED
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult.Companion.DELETE_MARKUP
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult.Companion.SUCCESS
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLProcess
import ru.kochkaev.zixamc.tgbridge.sql.callback.*
import ru.kochkaev.zixamc.tgbridge.sql.data.AccountType
import ru.kochkaev.zixamc.tgbridge.sql.util.*
import ru.kochkaev.zixamc.tgbridge.sql.process.GroupChatSyncWaitPrefixProcessData
import ru.kochkaev.zixamc.tgbridge.sql.process.GroupSelectTopicProcessData
import ru.kochkaev.zixamc.tgbridge.sql.process.GroupWaitingNameProcessData
import ru.kochkaev.zixamc.tgbridge.sql.process.ProcessTypes
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureTypes
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.TopicFeatureData
import ru.kochkaev.zixamc.tgbridge.telegram.feature.TopicFeatureType

object ServerBotGroup {

    val CAN_EXECUTE_ADMIN = CallbackCanExecute(
        statuses = listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR),
        display = config.integration.group.memberStatus.administrators,
    )
    val SETTINGS = TgMenu(listOf(
        listOf(SQLCallback.of(
            display = config.integration.group.settings.features,
            type = "group",
            data = GroupCallback(Operations.EDIT_FEATURES),
            canExecute = CAN_EXECUTE_ADMIN,
        )),
        listOf(SQLCallback.of(
            display = config.integration.group.settings.changeName,
            type = "group",
            data = GroupCallback(Operations.UPDATE_NAME),
            canExecute = CAN_EXECUTE_ADMIN,
        )),
        listOf(SQLCallback.of(
            display = config.integration.group.settings.aliases,
            type = "group",
            data = GroupCallback(Operations.GET_ALIASES),
            canExecute = CAN_EXECUTE_ADMIN,
        )),
    ))
    suspend fun newChatMembers(msg: TgMessage) {
        val group = SQLGroup.getOrCreate(msg.chat.id) ?: return
        msg.newChatMembers!!.forEach { member ->
            if (member.id == bot.me.id && msg.from != null) {
                val user = SQLEntity.get(msg.from.id)
                if (user == null || !user.hasProtectedLevel(AccountType.PLAYER)) {
                    bot.sendMessage(
                        chatId = msg.chat.id,
                        text = config.integration.group.sorryOnlyForPlayer,
                    )
                    bot.leaveChat(msg.chat.id)
                    return
                }
                else if (group.isRestricted) {
                    bot.sendMessage(
                        chatId = msg.chat.id,
                        text = config.integration.group.restrict,
                    )
                    bot.leaveChat(msg.chat.id)
                    return
                }
                bot.sendMessage(
                    chatId = msg.chat.id,
                    text = config.integration.group.hello
                )
                if (!group.agreedWithRules) {
                    bot.sendMessage(
                        chatId = msg.chat.id,
                        text = config.integration.group.needAgreeWithRules,
                        replyMarkup = TgMenu(
                            listOf(
                                listOf(
                                    SQLCallback.of(
                                        display = config.integration.group.agreeWithRules,
                                        type = "group",
                                        data = GroupCallback(Operations.AGREE_WITH_RULES),
                                        canExecute = CallbackCanExecute(
                                            statuses = listOf(TgChatMemberStatuses.CREATOR),
                                            display = config.integration.group.memberStatus.creator
                                        ),
                                    )
                                )
                            )
                        )
                    )
                }
            }
            else if (!member.isBot) {
                val user = SQLEntity.getOrCreate(member.id)
                group.cleanUpProtected(user.accountType)
                bot.sendMessage(
                    chatId = group.chatId,
                    text = config.integration.group.protectedWasDeleted
                )
            }
        }
    }
    suspend fun leftChatMember(msg: TgMessage) {
        val group = SQLGroup.get(msg.chat.id) ?: return
        val member = msg.leftChatMember!!
        if (member.id == bot.me.id) {
            group.features.setAll(hashMapOf())
            group.members.set(listOf())
            group.agreedWithRules = false
            try {
                group.deleteProtected(AccountType.UNKNOWN)
            } catch (_: Exception) {}
        }
        else if (!member.isBot) {
            val user = SQLEntity.get(member.id) ?: return
            group.members.remove(LinkedUser(user.userId))
        }
    }

    suspend fun onCallback(cbq: TgCallbackQuery, sql: SQLCallback<GroupCallback>): TgCBHandlerResult {
        if (sql.data == null) return SUCCESS
        val group = SQLGroup.get(cbq.message.chat.id) ?: return SUCCESS
        if (sql.canExecute?.statuses?.contains(bot.getChatMember(group.chatId, cbq.from.id).status) != true) return answerHaventRights(cbq.id, sql.canExecute?.display?:"")
        when (sql.data!!.operation) {
            Operations.AGREE_WITH_RULES -> {
                group.agreedWithRules = true
                if (group.name == null) {
                    val message = bot.sendMessage(
                        chatId = group.chatId,
                        text = config.integration.group.thinkOfName,
                        replyMarkup = TgMenu(
                            listOf(
                            cbq.message.chat.title.let { escapeName(it) }.let {
                                if (group.canTakeName(it)) listOf(
                                    SQLCallback.of(
                                        display = it,
                                        type = "group",
                                        data = GroupCallback(
                                            operation = Operations.SET_NAME,
                                            name = it
                                        ),
                                        canExecute = CAN_EXECUTE_ADMIN,
                                    )
                                ) else listOf()
                            },
                            cbq.message.chat.username?.let { escapeName(it) }?.let {
                                if (group.canTakeName(it)) listOf(
                                    SQLCallback.of(
                                        display = it,
                                        type = "group",
                                        data = GroupCallback(
                                            operation = Operations.SET_NAME,
                                            name = it
                                        ),
                                        canExecute = CAN_EXECUTE_ADMIN,
                                    )
                                ) else listOf()
                            } ?: listOf(),
                        ))
                    )
                    SQLProcess.get(group.chatId, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                        this.data?.messageId?.also { try {
                            bot.editMessageReplyMarkup(
                                chatId = group.chatId,
                                messageId = it,
                                replyMarkup = TgReplyMarkup()
                            )
                            SQLCallback.dropAll(group.chatId, it)
                        } catch (_: Exception) {} }
                    } ?.drop()
                    SQLProcess.of(
                        ProcessTypes.GROUP_WAITING_NAME, GroupWaitingNameProcessData(
                        messageId = message.messageId,
                        nameType = GroupWaitingNameProcessData.NameType.NAME,
                    )
                    ).pull(group.chatId)
                }
                else sendFeatures(group, cbq.message.messageId, true)
//                } else {
//                    bot.answerCallbackQuery(
//                        callbackQueryId = cbq.id,
//                        text = config.integration.group.needAgreeOwner,
//                        showAlert = true
//                    )
//                    return SUCCESS
//                }
            }
            Operations.SET_NAME -> {
                group.name = sql.data!!.name
                SQLProcess.get(group.chatId, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                    this.data?.messageId?.also { try {
                        bot.editMessageReplyMarkup(
                            chatId = group.chatId,
                            messageId = it,
                            replyMarkup = TgReplyMarkup()
                        )
                        SQLCallback.dropAll(group.chatId, it)
                    } catch (_: Exception) {} }
                } ?.drop()
                sendFeatures(group, cbq.message.messageId, true)
            }
            Operations.UPDATE_NAME -> {
                SQLProcess.get(group.chatId, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                    this.data?.messageId?.also { try {
                        bot.editMessageReplyMarkup(
                            chatId = group.chatId,
                            messageId = it,
                            replyMarkup = TgReplyMarkup()
                        )
                        SQLCallback.dropAll(group.chatId, it)
                    } catch (_: Exception) {} }
                } ?.drop()
                SQLProcess.of(
                    ProcessTypes.GROUP_WAITING_NAME, GroupWaitingNameProcessData(
                    messageId = cbq.message.messageId,
                    nameType = GroupWaitingNameProcessData.NameType.NAME
                )
                ).pull(group.chatId)
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = config.integration.group.thinkOfName,
                )
                bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(listOf(listOf(
                        CancelCallbackData(
                            asCallbackSend = CancelCallbackData.CallbackSend(
                                type = "group",
                                data = GroupCallback(Operations.SETTINGS),
                                result = DELETE_LINKED
                            ),
                            canExecute = CAN_EXECUTE_ADMIN,
                        ).build()
                    ))),
                )
                return DELETE_LINKED
            }
            Operations.ADD_ALIAS -> {
                SQLProcess.get(group.chatId, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                    this.data?.messageId?.also { try {
                        bot.editMessageReplyMarkup(
                            chatId = group.chatId,
                            messageId = it,
                            replyMarkup = TgReplyMarkup()
                        )
                        SQLCallback.dropAll(group.chatId, it)
                    } catch (_: Exception) {} }
                } ?.drop()
                SQLProcess.of(
                    ProcessTypes.GROUP_WAITING_NAME, GroupWaitingNameProcessData(
                    messageId = cbq.message.messageId,
                    nameType = GroupWaitingNameProcessData.NameType.ALIAS
                )
                ).pull(group.chatId)
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = config.integration.group.thinkOfName,
                )
                bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(listOf(listOf(
                        CancelCallbackData(
                            asCallbackSend = CancelCallbackData.CallbackSend(
                                type = "group",
                                data = GroupCallback(Operations.GET_ALIASES),
                                result = DELETE_LINKED
                            ),
                            canExecute = CAN_EXECUTE_ADMIN,
                        ).build()
                    ))),
                )
                return DELETE_LINKED
            }
            Operations.GET_ALIASES -> {
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = config.integration.group.settings.aliasesDescription,
                )
                bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        (group.aliases.get()?. let {
                            it.fold(arrayListOf<List<SQLCallback.Companion.Builder<out CallbackData>>>()) { acc, a ->
                                acc.add(listOf(SQLCallback.of(
                                    display = TextParser.formatLang(
                                        text = config.integration.group.settings.removeAlias,
                                        "alias" to a
                                    ),
                                    type = "group",
                                    data = GroupCallback(
                                        operation = Operations.REMOVE_ALIAS,
                                        name = a
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                )))
                                acc
                            }
                        } ?: arrayListOf()).apply {
                            this.add(listOf(SQLCallback.of(
                                display = config.integration.group.settings.addAlias,
                                type = "group",
                                data = GroupCallback(Operations.ADD_ALIAS),
                                canExecute = CAN_EXECUTE_ADMIN,
                            )))
                            this.add(listOf(
                                CancelCallbackData(
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback(Operations.SETTINGS),
                                        result = DELETE_LINKED
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                ).build()
                            ))
                        }
                    ),
                )
                return DELETE_LINKED
            }
            Operations.REMOVE_ALIAS -> {
                if (sql.data!!.name == null) return SUCCESS
                group.aliases.remove(sql.data!!.name!!)
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = TextParser.formatLang(
                        text = config.integration.group.settings.aliasDeleted,
                        "alias" to sql.data!!.name!!
                    ),
                )
                bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(listOf(
                        listOf(
                            CancelCallbackData(
                                asCallbackSend = CancelCallbackData.CallbackSend(
                                    type = "group",
                                    data = GroupCallback(Operations.GET_ALIASES),
                                    result = DELETE_LINKED
                                ),
                                canExecute = CAN_EXECUTE_ADMIN,
                            ).build()
                        ))
                    ),
                )
                return DELETE_LINKED
            }
            Operations.TOPIC_FEATURE -> {
                val feature = FeatureTypes.entries[sql.data!!.name] ?: return DELETE_MARKUP
                return feature.setUp(cbq, group)
            }
            Operations.SEND_FEATURES -> {
                sendFeatures(group, cbq.message.messageId, false)
                return DELETE_MARKUP
            }
            Operations.EDIT_FEATURES -> {
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = config.integration.group.settings.featuresDescription,
                )
                bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        (group.features.getAll()?.let {
                            it.keys.fold(arrayListOf<List<SQLCallback.Companion.Builder<out CallbackData>>>()) { acc, feature ->
                                acc.add(listOf(SQLCallback.of(
                                    display = feature.tgDisplayName(),
                                    type = "group",
                                    data = GroupCallback(
                                        operation = Operations.EDIT_FEATURE,
                                        name = feature.serializedName,
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                )))
                                acc
                            }
                        } ?: arrayListOf()).apply {
                            this.add(listOf(SQLCallback.of(
                                display = config.integration.group.settings.addFeature,
                                type = "group",
                                data = GroupCallback(Operations.SEND_FEATURES),
                                canExecute = CAN_EXECUTE_ADMIN,
                            )))
                            this.add(listOf(
                                CancelCallbackData(
                                asCallbackSend = CancelCallbackData.CallbackSend(
                                    type = "group",
                                    data = GroupCallback(Operations.SETTINGS),
                                    result = DELETE_LINKED
                                ),
                                canExecute = CAN_EXECUTE_ADMIN,
                            ).build()))
                        }),
                )
                return DELETE_LINKED
            }
            Operations.EDIT_FEATURE -> {
                if (sql.data!!.name == null) return SUCCESS
                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return SUCCESS
                val data = group.features.getCasted(type) ?: return SUCCESS
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = TextParser.formatLang(
                        text = config.integration.group.settings.featureDescription,
                        "feature" to type.tgDisplayName(),
                        "options" to type.getResolvedOptions(data),
                    ),
                )
                bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(listOf(
                        listOf(SQLCallback.of(
                            display = config.integration.group.settings.editFeature,
                            type = "group",
                            data = GroupCallback(
                                operation = Operations.EDIT_FEATURE_DATA,
                                name = type.serializedName,
                            ),
                            canExecute = CAN_EXECUTE_ADMIN,
                        )),
                        listOf(SQLCallback.of(
                            display = config.integration.group.settings.removeFeature,
                            type = "group",
                            data = GroupCallback(
                                operation = Operations.REMOVE_FEATURE,
                                name = type.serializedName,
                            ),
                            canExecute = CAN_EXECUTE_ADMIN,
                        )),
                        listOf(
                            CancelCallbackData(
                                asCallbackSend = CancelCallbackData.CallbackSend(
                                    type = "group",
                                    data = GroupCallback(Operations.EDIT_FEATURES),
                                    result = DELETE_LINKED
                                ),
                                canExecute = CAN_EXECUTE_ADMIN,
                            ).build()
                        ))
                    ),
                )
                return DELETE_LINKED
            }
            Operations.EDIT_FEATURE_DATA -> {
                if (sql.data!!.name == null) return SUCCESS
                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return SUCCESS
                return type.sendEditor(cbq, group)
            }
            Operations.CONFIRM_SETUP_FEATURE -> {
                if (sql.data!!.name == null) return SUCCESS
                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return SUCCESS
                return type.finishSetUp(group, cbq.message.messageId)
            }
            Operations.REMOVE_FEATURE -> {
                if (sql.data!!.name == null) return SUCCESS
                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return SUCCESS
                group.features.remove(type)
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = TextParser.formatLang(
                        text = config.integration.group.settings.featureRemoved,
                        "feature" to type.tgDisplayName()
                    ),
                )
                bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(listOf(
                        listOf(
                            CancelCallbackData(
                                asCallbackSend = CancelCallbackData.CallbackSend(
                                    type = "group",
                                    data = GroupCallback(Operations.EDIT_FEATURES),
                                    result = DELETE_LINKED
                                ),
                                canExecute = CAN_EXECUTE_ADMIN,
                            ).build()
                        ))
                    ),
                )
                return DELETE_LINKED
            }
            Operations.SETTINGS -> {
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = getSettingsText(group)
                )
                bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = SETTINGS
                )
                return DELETE_LINKED
            }
            Operations.TOPIC_RESELECT -> {
                if (sql.data!!.name == null) return SUCCESS
                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return SUCCESS
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = config.integration.group.selectTopicForFeature
                )
                SQLProcess.get(group.chatId, ProcessTypes.GROUP_SELECT_TOPIC_FEATURE)?.run {
                    this.data?.also { bot.deleteMessage(group.chatId, it.messageId) }
                    this.drop()
                }
                SQLProcess.of(ProcessTypes.GROUP_SELECT_TOPIC_FEATURE, GroupSelectTopicProcessData(cbq.message.messageId, type.serializedName)).pull(group.chatId)
                bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(listOf(
                        listOf(
                            CancelCallbackData(
                            cancelProcesses = listOf(ProcessTypes.GROUP_SELECT_TOPIC_FEATURE),
                            asCallbackSend = CancelCallbackData.CallbackSend(
                                type = "group",
                                data = GroupCallback(
                                    operation = Operations.EDIT_FEATURE_DATA,
                                    name = type.serializedName,
                                ),
                                result = DELETE_MARKUP,
                            ),
                            canExecute = CAN_EXECUTE_ADMIN,
                        ).build())
                    ))
                )
                return DELETE_LINKED
            }
            Operations.EDIT_PREFIX -> {
                if (sql.data!!.name == null) return SUCCESS
//                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return SUCCESS
                FeatureTypes.CHAT_SYNC.sendNeedPrefix(
                    group = group,
                    replyTo = cbq.message.messageId,
                    topicId = null,
                    prefixType = GroupChatSyncWaitPrefixProcessData.PrefixTypes.valueOf(sql.data!!.name!!)
                )
            }
        }
        return DELETE_MARKUP
    }

    suspend fun onMessage(msg: TgMessage) {
        val group = SQLGroup.get(msg.chat.id) ?: return
        val processes = SQLProcess.getAll(group.chatId)
        processes.forEach { process ->
            when (process.type) {
                ProcessTypes.GROUP_WAITING_NAME ->
                    if (msg.replyToMessage?.from?.id == bot.me.id && msg.from != null &&
                        listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(
                            bot.getChatMember(group.chatId, msg.from.id).status
                        )
                    ) {
                        val inFirstTime = group.name == null
                        val data = process.data as GroupWaitingNameProcessData
                        if (data.messageId != msg.replyToMessage.messageId) return
                        val name = msg.effectiveText ?: return
                        if (name.length !in 1..16 || !name.matches(Regex("[а-яa-z0-9_\\-]+", RegexOption.IGNORE_CASE))) {
                            bot.sendMessage(
                                chatId = group.chatId,
                                text = config.integration.group.incorrectName,
                                replyParameters = TgReplyParameters(
                                    msg.messageId
                                )
                            )
                        } else if (!group.canTakeName(name)) {
                            bot.sendMessage(
                                chatId = group.chatId,
                                text = config.integration.group.nameIsTaken,
                                replyParameters = TgReplyParameters(
                                    msg.messageId
                                )
                            )
                        } else {
                            if (data.nameType == GroupWaitingNameProcessData.NameType.NAME)
                                group.name = name
                            else if (data.nameType == GroupWaitingNameProcessData.NameType.ALIAS && !group.aliases.contains(name))
                                group.aliases.add(name)
                            bot.editMessageReplyMarkup(
                                chatId = group.chatId,
                                messageId = data.messageId,
                                replyMarkup = TgReplyMarkup(),
                            )
                            SQLCallback.dropAll(group.chatId, data.messageId)
                            process.drop()
                            if (inFirstTime) sendFeatures(group, msg.messageId, true)
                            else settingsCommand(msg)
                        }
                    }
                ProcessTypes.GROUP_CHATSYNC_WAITING_PREFIX ->
                    if (msg.replyToMessage?.from?.id == bot.me.id && msg.from != null &&
                        listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(
                            bot.getChatMember(group.chatId, msg.from.id).status
                        )
                    ) {
                        val data = process.data as GroupChatSyncWaitPrefixProcessData
                        if (data.messageId != msg.replyToMessage.messageId) return
                        val isNotNew = group.features.contains(FeatureTypes.CHAT_SYNC)
                        val prefix = msg.effectiveText?:return
                        val mm = TextData(prefix)
                        try {
                            mm.get()
                        } catch (e: Exception) {
                            bot.sendMessage(
                                chatId = group.chatId,
                                text = TextParser.formatLang(
                                    text = config.integration.group.features.chatSync.wrongPrefix,
                                    "error" to e.message.toString()
                                )
                            )
                            return
                        }
                        group.features.set(
                            FeatureTypes.CHAT_SYNC,
                            if (!isNotNew)
                                FeatureTypes.CHAT_SYNC.getDefault().apply {
                                    this.topicId = data.topicId
                                    this.prefix = mm
                                }
                            else group.features.getCasted(FeatureTypes.CHAT_SYNC)!!.apply {
                                if (data.type == GroupChatSyncWaitPrefixProcessData.PrefixTypes.DEFAULT)
                                    this.prefix = mm
                                else if (data.type == GroupChatSyncWaitPrefixProcessData.PrefixTypes.FROM_MINECRAFT)
                                    this.fromMcPrefix = mm
                            }
                        )
                        try { bot.editMessageReplyMarkup(
                            chatId = group.chatId,
                            messageId = data.messageId,
                            replyMarkup = TgReplyMarkup()
                        ) } catch (_: Exception) {}
                        SQLCallback.dropAll(group.chatId, data.messageId)
                        process.drop()
                        if (isNotNew) {
                            bot.sendMessage(
                                chatId = group.chatId,
                                text = getSettingsText(group),
                                replyMarkup = SETTINGS,
                            )
                        } else bot.sendMessage(
                            chatId = group.chatId,
                            text = if (msg.chat.isForum) config.integration.group.features.chatSync.doneTopic
                                else config.integration.group.features.chatSync.doneNoTopic,
                            replyParameters = TgReplyParameters(
                                msg.messageId
                            )
                        )
                    }
            }
        }
    }

    suspend fun answerHaventRights(id: String, display: String): TgCBHandlerResult {
        bot.answerCallbackQuery(
            callbackQueryId = id,
            text = TextParser.formatLang(
                text = config.integration.group.haveNotPermission,
                "placeholder" to display
            ),
            showAlert = true
        )
        return SUCCESS
    }
    suspend fun selectTopicCommand(msg: TgMessage) {
        val group = SQLGroup.get(msg.chat.id)?:return
        if (!(msg.from != null &&
            listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(
                bot.getChatMember(group.chatId, msg.from.id).status
            )
        )) return
        val processes = SQLProcess.getAll(group.chatId)
        processes.forEach { process ->
            when (process.type) {
                ProcessTypes.GROUP_SELECT_TOPIC_FEATURE -> {
                    val data = process.data as GroupSelectTopicProcessData
                    val topicId = msg.messageThreadId
                    val feature = FeatureTypes.entries[data.feature] ?: return
                    if (feature !is TopicFeatureType) return
                    if (group.features.contains(feature)) {
                        group.features.set(
                            key = feature,
                            value = (group.features.getCasted(feature) as TopicFeatureData).apply {
                                this.topicId = topicId
                            }
                        )
                        bot.sendMessage(
                            chatId = group.chatId,
                            text = getSettingsText(group),
                            replyMarkup = SETTINGS,
                            replyParameters = TgReplyParameters(
                                msg.messageId
                            ),
                        )
                    }
                    else feature.finishSetUp(group, topicId, topicId)
                    try { bot.editMessageReplyMarkup(
                        chatId = group.chatId,
                        messageId = data.messageId,
                        replyMarkup = TgReplyMarkup(),
                    ) } catch (_: Exception) {}
                    process.drop()
                }
            }
        }
    }
    suspend fun settingsCommand(msg: TgMessage) {
        val group = SQLGroup.get(msg.chat.id)?:return
        if (msg.from == null ||
            !listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(
                bot.getChatMember(group.chatId, msg.from.id).status
            )
        ) return
        bot.sendMessage(
            chatId = group.chatId,
            text = getSettingsText(group),
            replyParameters = TgReplyParameters(msg.messageId),
            replyMarkup = SETTINGS
        )
    }

    suspend fun sendFeatures(group: SQLGroup, replyTo: Int? = null, withDone: Boolean = false) {
        val message = (if (withDone) config.integration.group.done + "\n" else "") + resolveFeaturesSettingsMessage(group)
        val menu = TgMenu(
            FeatureTypes.entries
                .map { it.value }
                .filter { it.checkAvailable(group) }
                .map { SQLCallback.of(
                    display = it.tgDisplayName(),
                    type = "group",
                    data = GroupCallback(
                        operation = Operations.TOPIC_FEATURE,
                        name = it.serializedName
                    ),
                    canExecute = CAN_EXECUTE_ADMIN,
                ) }
                .map { listOf(it) }
        )
        bot.sendMessage(
            chatId = group.chatId,
            text = message,
            replyParameters = replyTo?.let { TgReplyParameters(it) },
            replyMarkup = menu
        )
    }
    fun getSettingsText(group: SQLGroup) =
        TextParser.formatLang(
            text = config.integration.group.settings.text,
            "groupName" to (group.name ?: config.integration.group.settings.nullPlaceholder)
        )

    fun escapeName(current: String) =
        current.replace(" ", "_").replace(Regex("[^а-яa-z0-9_\\-]", RegexOption.IGNORE_CASE), "")
    suspend fun resolveFeaturesSettingsMessage(group: SQLGroup) =
        config.integration.group.groupHasNoOnlyPlayers.let { if (!group.hasProtectedLevel(AccountType.PLAYER)) it else "" } +
        "\n" + config.integration.group.selectFeature

    data class GroupCallback(
        var operation: Operations,
        var name: String? = null,
    ): CallbackData
    enum class Operations {
        @SerializedName("agree_with_rules")
        AGREE_WITH_RULES,
        @SerializedName("set_name")
        SET_NAME,
        @SerializedName("update_name")
        UPDATE_NAME,
        @SerializedName("get_aliases")
        GET_ALIASES,
        @SerializedName("remove_alias")
        REMOVE_ALIAS,
        @SerializedName("add_alias")
        ADD_ALIAS,
        @SerializedName("topic_feature")
        TOPIC_FEATURE,
        @SerializedName("send_features")
        SEND_FEATURES,
        @SerializedName("edit_features")
        EDIT_FEATURES,
        @SerializedName("edit_feature")
        EDIT_FEATURE,
        @SerializedName("edit_feature_data")
        EDIT_FEATURE_DATA,
        @SerializedName("remove_feature")
        REMOVE_FEATURE,
        @SerializedName("settings")
        SETTINGS,
        @SerializedName("topic_reselect")
        TOPIC_RESELECT,
        @SerializedName("edit_prefix")
        EDIT_PREFIX,
        @SerializedName("confirm_setup_feature")
        CONFIRM_SETUP_FEATURE,
    }
}