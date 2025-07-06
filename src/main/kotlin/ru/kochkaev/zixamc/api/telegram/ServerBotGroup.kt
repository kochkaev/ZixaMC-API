package ru.kochkaev.zixamc.api.telegram

import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLProcess
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.callback.CallbackCanExecute
import ru.kochkaev.zixamc.api.sql.callback.CallbackData
import ru.kochkaev.zixamc.api.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataTypes
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.feature.FeatureType
import ru.kochkaev.zixamc.api.sql.feature.FeatureTypes
import ru.kochkaev.zixamc.api.sql.feature.TopicFeatureType
import ru.kochkaev.zixamc.api.sql.feature.data.FeatureData
import ru.kochkaev.zixamc.api.sql.feature.data.TopicFeatureData
import ru.kochkaev.zixamc.chatsync.GroupChatSyncWaitPrefixProcessData
import ru.kochkaev.zixamc.api.sql.process.GroupSelectTopicProcessData
import ru.kochkaev.zixamc.api.sql.process.GroupWaitingNameProcessData
import ru.kochkaev.zixamc.api.sql.process.ProcessTypes
import ru.kochkaev.zixamc.api.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.api.telegram.model.TgChatMemberStatuses
import ru.kochkaev.zixamc.api.telegram.model.TgChatType
import ru.kochkaev.zixamc.api.telegram.model.TgMessage
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters
import ru.kochkaev.zixamc.chatsync.ChatSyncFeatureType
import ru.kochkaev.zixamc.requests.RequestsBot
import kotlin.collections.get

object ServerBotGroup {

    val CAN_EXECUTE_ADMIN = CallbackCanExecute(
        statuses = listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR),
        display = ServerBot.config.integration.group.memberStatus.administrators,
    )
    val CAN_EXECUTE_OWNER = CallbackCanExecute(
        statuses = listOf(TgChatMemberStatuses.CREATOR),
        display = ServerBot.config.integration.group.memberStatus.creator,
    )
    val SETTINGS = TgMenu(
        listOf(
            listOf(
                SQLCallback.Companion.of(
                    display = ServerBot.config.integration.group.settings.features,
                    type = "group",
                    data = GroupCallback(Operations.EDIT_FEATURES),
                    canExecute = CAN_EXECUTE_ADMIN,
                )
            ),
            listOf(
                SQLCallback.Companion.of(
                    display = ServerBot.config.integration.group.settings.changeName,
                    type = "group",
                    data = GroupCallback(Operations.UPDATE_NAME),
                    canExecute = CAN_EXECUTE_ADMIN,
                )
            ),
            listOf(
                SQLCallback.Companion.of(
                    display = ServerBot.config.integration.group.settings.aliases,
                    type = "group",
                    data = GroupCallback(Operations.GET_ALIASES),
                    canExecute = CAN_EXECUTE_ADMIN,
                )
            ),
            listOf(
                SQLCallback.Companion.of(
                    display = ServerBot.config.integration.group.removeAgreeWithRules,
                    type = "group",
                    data = GroupCallback(Operations.REMOVE_AGREE_WITH_RULES),
                    canExecute = CAN_EXECUTE_ADMIN,
                )
            ),
            listOf(
                SQLCallback.Companion.of(
                    display = ServerBot.config.integration.group.success,
                    type = "group",
                    data = GroupCallback(Operations.SUCCESS),
                    canExecute = CAN_EXECUTE_ADMIN,
                )
            ),
        )
    )
    suspend fun newChatMembers(msg: TgMessage) {
        if (msg.chat.type == TgChatType.CHANNEL) {
            ServerBot.bot.leaveChat(msg.chat.id)
        }
        val group = SQLGroup.Companion.getOrCreate(msg.chat.id)
        msg.newChatMembers!!.forEach { member ->
            if (member.id == ServerBot.bot.me.id && msg.from != null) {
                try {
                    group.deleteProtected(AccountType.UNKNOWN)
                } catch (_: Exception) {}
                SQLCallback.Companion.getAll(group.chatId).sortedByDescending { it.messageId }.forEach {
                    it.messageId?.also { messageId -> try {
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.chatId,
                            messageId = messageId,
                            replyMarkup = TgReplyMarkup()
                        )
                    } catch (_: Exception) { } }
                    it.drop()
                }
                val user = SQLUser.Companion.get(msg.from.id)
                if (user == null || !user.hasProtectedLevel(AccountType.PLAYER)) {
                    ServerBot.bot.sendMessage(
                        chatId = msg.chat.id,
                        text = ServerBot.config.integration.group.sorryOnlyForPlayer,
                    )
                    ServerBot.bot.leaveChat(msg.chat.id)
                    return
                }
                else if (group.isRestricted) {
                    ServerBot.bot.sendMessage(
                        chatId = msg.chat.id,
                        text = ServerBot.config.integration.group.restrict,
                    )
                    ServerBot.bot.leaveChat(msg.chat.id)
                    return
                }
                ServerBot.bot.sendMessage(
                    chatId = msg.chat.id,
                    text = ServerBot.config.integration.group.hello
                )
                if (!group.agreedWithRules) {
                    ServerBot.bot.sendMessage(
                        chatId = msg.chat.id,
                        text = ServerBot.config.integration.group.needAgreeWithRules,
                        replyMarkup = TgMenu(
                            listOf(
                                listOf(
                                    SQLCallback.Companion.of(
                                        display = ServerBot.config.integration.group.agreeWithRules,
                                        type = "group",
                                        data = GroupCallback(Operations.AGREE_WITH_RULES),
                                        canExecute = CAN_EXECUTE_OWNER,
                                    )
                                )
                            )
                        )
                    )
                }
                else sendFeatures(group)
            }
            else {
                group.members.add(member.id)
                if (!member.isBot) {
                    val user = SQLUser.Companion.getOrCreate(member.id)
                    group.cleanUpProtected(user.accountType)
                    if (group.data.getCasted(ChatDataTypes.GREETING_ENABLE)?:false) ServerBot.bot.sendMessage(
                        chatId = group.chatId,
                        text = ServerBot.config.integration.group.protectedWasDeleted
                    )
                }
            }
        }
    }
    suspend fun newChatMembersRequests(msg: TgMessage) {
        val user = msg.from?.let { SQLUser.Companion.get(it.id) } ?: return
        val ids = msg.newChatMembers?.map { it.id } ?: return
        BotLogic.bots.forEach {
            if (ids.contains(it.me.id) && !user.hasProtectedLevel(it.canAddToGroups))
                it.leaveChat(msg.chat.id)
        }
    }
    suspend fun leftChatMember(msg: TgMessage) {
        val group = SQLGroup.Companion.get(msg.chat.id) ?: return
        val member = msg.leftChatMember!!
        if (member.id == ServerBot.bot.me.id) {
            group.features.setAll(hashMapOf())
            group.members.set(listOf())
            group.agreedWithRules = false
        }
        else {
            group.members.remove(member.id)
            if (!group.atLeastOnePlayer()) group.onNoMorePlayers()
        }
    }

    suspend fun onCallback(cbq: TgCallbackQuery, sql: SQLCallback<GroupCallback>): TgCBHandlerResult {
        if (sql.data == null) return TgCBHandlerResult.Companion.SUCCESS
        val group = SQLGroup.Companion.get(cbq.message.chat.id) ?: return TgCBHandlerResult.Companion.SUCCESS
        if (sql.canExecute?.let {
                !(it.statuses?.contains(ServerBot.bot.getChatMember(group.chatId, cbq.from.id).status) == true || it.users?.contains(cbq.from.id) == true)
        } != false) return answerHaventRights(cbq.id, sql.canExecute?.display?:"")
        when (sql.data!!.operation) {
            Operations.AGREE_WITH_RULES -> {
                group.agreedWithRules = true
                if (group.name == null) {
                    val message = ServerBot.bot.sendMessage(
                        chatId = group.chatId,
                        text = ServerBot.config.integration.group.thinkOfName,
                        replyMarkup = TgMenu(
                            listOf(
                                cbq.message.chat.title.let { escapeName(it) }.let {
                                    if (group.canTakeName(it)) listOf(
                                        SQLCallback.Companion.of(
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
                                        SQLCallback.Companion.of(
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
                    SQLProcess.Companion.get(group.chatId, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                        this.data?.messageId?.also { try {
                            ServerBot.bot.editMessageReplyMarkup(
                                chatId = group.chatId,
                                messageId = it,
                                replyMarkup = TgReplyMarkup()
                            )
                            SQLCallback.Companion.dropAll(group.chatId, it)
                        } catch (_: Exception) {} }
                    } ?.drop()
                    SQLProcess.Companion.of(
                        ProcessTypes.GROUP_WAITING_NAME, GroupWaitingNameProcessData(
                            messageId = message.messageId,
                            nameType = GroupWaitingNameProcessData.NameType.NAME,
                        )
                    ).pull(group.chatId)
                }
                else sendFeatures(group, cbq.message.messageId, true, null)
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
                SQLProcess.Companion.get(group.chatId, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                    this.data?.messageId?.also { try {
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.chatId,
                            messageId = it,
                            replyMarkup = TgReplyMarkup()
                        )
                        SQLCallback.Companion.dropAll(group.chatId, it)
                    } catch (_: Exception) {} }
                } ?.drop()
                sendFeatures(group, cbq.message.messageId, true, null)
            }
            Operations.UPDATE_NAME -> {
                SQLProcess.Companion.get(group.chatId, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                    this.data?.messageId?.also { try {
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.chatId,
                            messageId = it,
                            replyMarkup = TgReplyMarkup()
                        )
                        SQLCallback.Companion.dropAll(group.chatId, it)
                    } catch (_: Exception) {} }
                } ?.drop()
                SQLProcess.Companion.of(
                    ProcessTypes.GROUP_WAITING_NAME, GroupWaitingNameProcessData(
                        messageId = cbq.message.messageId,
                        nameType = GroupWaitingNameProcessData.NameType.NAME
                    )
                ).pull(group.chatId)
                ServerBot.bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.integration.group.thinkOfName,
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                CancelCallbackData(
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback(Operations.SETTINGS),
                                        result = TgCBHandlerResult.Companion.DELETE_LINKED
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                ).build()
                            )
                        )
                    ),
                )
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.ADD_ALIAS -> {
                SQLProcess.Companion.get(group.chatId, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                    this.data?.messageId?.also { try {
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.chatId,
                            messageId = it,
                            replyMarkup = TgReplyMarkup()
                        )
                        SQLCallback.Companion.dropAll(group.chatId, it)
                    } catch (_: Exception) {} }
                } ?.drop()
                SQLProcess.Companion.of(
                    ProcessTypes.GROUP_WAITING_NAME, GroupWaitingNameProcessData(
                        messageId = cbq.message.messageId,
                        nameType = GroupWaitingNameProcessData.NameType.ALIAS
                    )
                ).pull(group.chatId)
                ServerBot.bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.integration.group.thinkOfName,
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                CancelCallbackData(
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback(Operations.GET_ALIASES),
                                        result = TgCBHandlerResult.Companion.DELETE_LINKED
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                ).build()
                            )
                        )
                    ),
                )
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.GET_ALIASES -> {
                ServerBot.bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.integration.group.settings.aliasesDescription,
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        (group.aliases.get()?.let {
                            it.fold(arrayListOf<List<SQLCallback.Companion.Builder<out CallbackData>>>()) { acc, a ->
                                acc.add(
                                    listOf(
                                        SQLCallback.Companion.of(
                                            display = ServerBot.config.integration.group.settings.removeAlias.formatLang(
                                                "alias" to a
                                            ),
                                            type = "group",
                                            data = GroupCallback(
                                                operation = Operations.REMOVE_ALIAS,
                                                name = a
                                            ),
                                            canExecute = CAN_EXECUTE_ADMIN,
                                        )
                                    )
                                )
                                acc
                            }
                        } ?: arrayListOf()).apply {
                            this.add(
                                listOf(
                                    SQLCallback.Companion.of(
                                        display = ServerBot.config.integration.group.settings.addAlias,
                                        type = "group",
                                        data = GroupCallback(Operations.ADD_ALIAS),
                                        canExecute = CAN_EXECUTE_ADMIN,
                                    )
                                )
                            )
                            this.add(
                                listOf(
                                    CancelCallbackData(
                                        asCallbackSend = CancelCallbackData.CallbackSend(
                                            type = "group",
                                            data = GroupCallback(Operations.SETTINGS),
                                            result = TgCBHandlerResult.Companion.DELETE_LINKED
                                        ),
                                        canExecute = CAN_EXECUTE_ADMIN,
                                    ).build()
                                )
                            )
                        }
                    ),
                )
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.REMOVE_ALIAS -> {
                if (sql.data!!.name == null) return TgCBHandlerResult.Companion.SUCCESS
                group.aliases.remove(sql.data!!.name!!)
                ServerBot.bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.integration.group.settings.aliasDeleted.formatLang(
                        "alias" to sql.data!!.name!!
                    ),
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                CancelCallbackData(
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback(Operations.GET_ALIASES),
                                        result = TgCBHandlerResult.Companion.DELETE_LINKED
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                ).build()
                            )
                        )
                    ),
                )
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.SELECT_FEATURE -> {
                val feature = FeatureTypes.entries[sql.data!!.name] ?: return TgCBHandlerResult.Companion.DELETE_MARKUP
                return feature.setUp(cbq, group)
            }
            Operations.SEND_FEATURES -> {
                sendFeatures(group, null, false, cbq.message.messageId)
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.EDIT_FEATURES -> {
                ServerBot.bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.integration.group.settings.featuresDescription,
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        (group.features.getAll()?.let {
                            it.keys.fold(arrayListOf<List<SQLCallback.Companion.Builder<out CallbackData>>>()) { acc, feature ->
                                acc.add(
                                    listOf(
                                        SQLCallback.Companion.of(
                                            display = feature.tgDisplayName(),
                                            type = "group",
                                            data = GroupCallback(
                                                operation = Operations.EDIT_FEATURE,
                                                name = feature.serializedName,
                                            ),
                                            canExecute = CAN_EXECUTE_ADMIN,
                                        )
                                    )
                                )
                                acc
                            }
                        } ?: arrayListOf()).apply {
                            this.add(
                                listOf(
                                    SQLCallback.Companion.of(
                                        display = ServerBot.config.integration.group.settings.addFeature,
                                        type = "group",
                                        data = GroupCallback(Operations.SEND_FEATURES),
                                        canExecute = CAN_EXECUTE_ADMIN,
                                    )
                                )
                            )
                            this.add(
                                listOf(
                                    CancelCallbackData(
                                        asCallbackSend = CancelCallbackData.CallbackSend(
                                            type = "group",
                                            data = GroupCallback(Operations.SETTINGS),
                                            result = TgCBHandlerResult.Companion.DELETE_LINKED
                                        ),
                                        canExecute = CAN_EXECUTE_ADMIN,
                                    ).build()
                                )
                            )
                        }),
                )
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.EDIT_FEATURE -> {
                if (sql.data!!.name == null) return TgCBHandlerResult.Companion.SUCCESS
                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return TgCBHandlerResult.Companion.SUCCESS
                val data = group.features.getCasted(type) ?: return TgCBHandlerResult.Companion.SUCCESS
                ServerBot.bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.integration.group.settings.featureDescription.formatLang(
                        "feature" to type.tgDisplayName(),
                        "options" to type.getResolvedOptions(data),
                    ),
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                SQLCallback.Companion.of(
                                    display = ServerBot.config.integration.group.settings.editFeature,
                                    type = "group",
                                    data = GroupCallback(
                                        operation = Operations.EDIT_FEATURE_DATA,
                                        name = type.serializedName,
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                )
                            ),
                            listOf(
                                SQLCallback.Companion.of(
                                    display = ServerBot.config.integration.group.settings.removeFeature,
                                    type = "group",
                                    data = GroupCallback(
                                        operation = Operations.REMOVE_FEATURE,
                                        name = type.serializedName,
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                )
                            ),
                            listOf(
                                CancelCallbackData(
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback(Operations.EDIT_FEATURES),
                                        result = TgCBHandlerResult.Companion.DELETE_LINKED
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                ).build()
                            )
                        )
                    ),
                )
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.EDIT_FEATURE_DATA -> {
                if (sql.data!!.name == null) return TgCBHandlerResult.Companion.SUCCESS
                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return TgCBHandlerResult.Companion.SUCCESS
                return type.sendEditor(cbq, group)
            }
            Operations.CONFIRM_SETUP_FEATURE -> {
                if (sql.data!!.name == null) return TgCBHandlerResult.Companion.SUCCESS
                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return TgCBHandlerResult.Companion.SUCCESS
                return type.finishSetUp(group, cbq.message.messageId)
            }
            Operations.REMOVE_FEATURE -> {
                if (sql.data!!.name == null) return TgCBHandlerResult.Companion.SUCCESS
                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return TgCBHandlerResult.Companion.SUCCESS
                group.features.remove(type)
                ServerBot.bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.integration.group.settings.featureRemoved.formatLang(
                        "feature" to type.tgDisplayName()
                    ),
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                CancelCallbackData(
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback(Operations.EDIT_FEATURES),
                                        result = TgCBHandlerResult.Companion.DELETE_LINKED
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                ).build()
                            )
                        )
                    ),
                )
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.SETTINGS -> {
                ServerBot.bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = getSettingsText(group)
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = SETTINGS
                )
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.TOPIC_RESELECT -> {
                if (sql.data!!.name == null) return TgCBHandlerResult.Companion.SUCCESS
                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return TgCBHandlerResult.Companion.SUCCESS
                ServerBot.bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.integration.group.selectTopicForFeature
                )
                SQLProcess.Companion.get(group.chatId, ProcessTypes.GROUP_SELECT_TOPIC_FEATURE)?.run {
                    this.data?.also { ServerBot.bot.deleteMessage(group.chatId, it.messageId) }
                    this.drop()
                }
                SQLProcess.Companion.of(
                    ProcessTypes.GROUP_SELECT_TOPIC_FEATURE,
                    GroupSelectTopicProcessData(cbq.message.messageId, type.serializedName)
                ).pull(group.chatId)
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                CancelCallbackData(
                                    cancelProcesses = listOf(ProcessTypes.GROUP_SELECT_TOPIC_FEATURE),
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback(
                                            operation = Operations.EDIT_FEATURE_DATA,
                                            name = type.serializedName,
                                        ),
                                        result = TgCBHandlerResult.Companion.DELETE_LINKED,
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                ).build()
                            )
                        )
                    )
                )
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.EDIT_PREFIX -> {
                if (sql.data!!.name == null) return TgCBHandlerResult.Companion.SUCCESS
//                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return SUCCESS
                ChatSyncFeatureType.sendNeedPrefix(
                    group = group,
                    replyTo = cbq.message.messageId,
                    topicId = null,
                    prefixType = GroupChatSyncWaitPrefixProcessData.PrefixTypes.valueOf(sql.data!!.name!!)
                )
            }
            Operations.REMOVE_AGREE_WITH_RULES -> {
                ServerBot.bot.sendMessage(
                    chatId = group.chatId,
                    text = ServerBot.config.integration.group.removeAgreeWithRulesAreYouSure,
                    replyParameters = TgReplyParameters(cbq.message.messageId),
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                SQLCallback.Companion.of(
                                    display = ServerBot.config.integration.group.confirm,
                                    type = "group",
                                    data = GroupCallback(Operations.CONFIRM_REMOVE_AGREE_WITH_RULES),
                                    canExecute = CAN_EXECUTE_OWNER
                                )
                            ),
                            listOf(
                                SQLCallback.Companion.of(
                                    display = ServerBot.config.integration.group.cancelConfirm,
                                    type = "group",
                                    data = GroupCallback(Operations.CANCEL_REMOVE_AGREE_WITH_RULES),
                                    canExecute = CAN_EXECUTE_OWNER
                                )
                            ),
                        )
                    )
                )
                return TgCBHandlerResult.Companion.SUCCESS
            }
            Operations.CONFIRM_REMOVE_AGREE_WITH_RULES -> {
                ServerBot.bot.sendMessage(
                    chatId = group.chatId,
                    text = ServerBot.config.integration.group.wait,
                    replyParameters = TgReplyParameters(cbq.message.messageId)
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgReplyMarkup()
                )
                val sanitized = arrayListOf<Int>()
                SQLCallback.Companion.getAll(group.chatId).sortedByDescending { it.messageId } .forEach {
                    it.messageId?.also { messageId -> if (!sanitized.contains(messageId)) try {
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.chatId,
                            messageId = messageId,
                            replyMarkup = TgReplyMarkup()
                        )
                        sanitized.add(messageId)
                    } catch (_: Exception) { } }
                    it.drop()
                }
                try {
                    group.deleteProtected(AccountType.UNKNOWN)
                } catch (_: Exception) {}
                group.members.set(listOf())
                group.features.setAll(mapOf())
                group.agreedWithRules = false
                ServerBot.bot.leaveChat(group.chatId)
                return TgCBHandlerResult.Companion.SUCCESS
            }
            Operations.CANCEL_REMOVE_AGREE_WITH_RULES -> {
                ServerBot.bot.deleteMessage(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                )
            }
            Operations.SETUP_FEATURE -> {
                val data = sql.data!!
                if (data is FeatureGroupCallback<out FeatureData>) {
                    return data.data.feature.uncheckedProcessSetup(cbq, group, data)
                } else return TgCBHandlerResult.Companion.SUCCESS
            }
            Operations.SUCCESS -> {
                ServerBot.bot.deleteMessage(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                )
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
        }
        return TgCBHandlerResult.Companion.DELETE_MARKUP
    }

    suspend fun waitNameProcessor(msg: TgMessage, process: SQLProcess<GroupWaitingNameProcessData>, data: GroupWaitingNameProcessData) {
        val group = SQLGroup.Companion.get(msg.chat.id) ?: return
        if (msg.replyToMessage?.from?.id == ServerBot.bot.me.id && msg.from != null &&
            listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(
                ServerBot.bot.getChatMember(group.chatId, msg.from.id).status
            )
        ) {
            val inFirstTime = group.name == null
//            val data = processData as GroupWaitingNameProcessData
            if (data.messageId != msg.replyToMessage.messageId) return
            val name = msg.effectiveText ?: return
            if (name.length !in 1..16 || !name.matches(Regex("[а-яa-z0-9_\\-]+", RegexOption.IGNORE_CASE))) {
                ServerBot.bot.sendMessage(
                    chatId = group.chatId,
                    text = ServerBot.config.integration.group.incorrectName,
                    replyParameters = TgReplyParameters(
                        msg.messageId
                    )
                )
            } else if (!group.canTakeName(name)) {
                ServerBot.bot.sendMessage(
                    chatId = group.chatId,
                    text = ServerBot.config.integration.group.nameIsTaken,
                    replyParameters = TgReplyParameters(
                        msg.messageId
                    )
                )
            } else {
                if (data.nameType == GroupWaitingNameProcessData.NameType.NAME)
                    group.name = name
                else if (data.nameType == GroupWaitingNameProcessData.NameType.ALIAS && !group.aliases.contains(name))
                    group.aliases.add(name)
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = data.messageId,
                    replyMarkup = TgReplyMarkup(),
                )
                SQLCallback.Companion.dropAll(group.chatId, data.messageId)
                process.drop()
                if (inFirstTime) sendFeatures(group, msg.messageId, true, null)
                else settingsCommand(msg)
            }
        }

    }

    suspend fun answerHaventRights(id: String, display: String): TgCBHandlerResult {
        ServerBot.bot.answerCallbackQuery(
            callbackQueryId = id,
            text = ServerBot.config.integration.group.haveNotPermission.formatLang(
                "placeholder" to display
            ),
            showAlert = true
        )
        return TgCBHandlerResult.Companion.SUCCESS
    }
    suspend fun selectTopicCommand(msg: TgMessage) {
        val group = SQLGroup.Companion.get(msg.chat.id)?:return
        if (!(msg.from != null &&
            listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(
                ServerBot.bot.getChatMember(group.chatId, msg.from.id).status
            )
        )) return
        val processes = SQLProcess.Companion.getAll(group.chatId)
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
                        ServerBot.bot.sendMessage(
                            chatId = group.chatId,
                            text = getSettingsText(group),
                            replyMarkup = SETTINGS,
                            replyParameters = TgReplyParameters(
                                msg.messageId
                            ),
                        )
                    }
                    else feature.finishSetUp(group, topicId, topicId)
                    try {
                        SQLCallback.Companion.getAll(group.chatId, data.messageId).forEach { it.drop() }
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.chatId,
                            messageId = data.messageId,
                            replyMarkup = TgReplyMarkup(),
                        )
                    } catch (_: Exception) {}
                    process.drop()
                }
            }
        }
    }
    suspend fun settingsCommand(msg: TgMessage) {
        val group = SQLGroup.Companion.get(msg.chat.id)?:return
        if (msg.from == null ||
            !listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(
                ServerBot.bot.getChatMember(group.chatId, msg.from.id).status
            )
        ) return
        ServerBot.bot.sendMessage(
            chatId = group.chatId,
            text = getSettingsText(group),
            replyParameters = TgReplyParameters(msg.messageId),
            replyMarkup = SETTINGS
        )
    }

    suspend fun sendFeatures(group: SQLGroup, replyTo: Int? = null, withDone: Boolean = false, edit: Int? = null) {
        val message = (if (withDone) ServerBot.config.integration.group.done + "\n" else "") + resolveFeaturesSettingsMessage(group)
        val menu = TgMenu(
            FeatureTypes.entries
                .map { it.value }
                .filter { it.checkAvailable(group) }
                .filter { !group.features.contains(it) }
                .map {
                    SQLCallback.Companion.of(
                        display = it.tgDisplayName(),
                        type = "group",
                        data = GroupCallback(
                            operation = Operations.SELECT_FEATURE,
                            name = it.serializedName
                        ),
                        canExecute = CAN_EXECUTE_ADMIN,
                    )
                }
                .map { listOf(it) }
        )
        if (edit == null) ServerBot.bot.sendMessage(
            chatId = group.chatId,
            text = message,
            replyParameters = replyTo?.let { TgReplyParameters(it) },
            replyMarkup = menu
        )
        else {
            ServerBot.bot.editMessageText(
                chatId = group.chatId,
                messageId = edit,
                text = message,
            )
            ServerBot.bot.editMessageReplyMarkup(
                chatId = group.chatId,
                messageId = edit,
                replyMarkup = menu
            )
        }
    }
    fun getSettingsText(group: SQLGroup) =
        ServerBot.config.integration.group.settings.text.formatLang(
            "groupName" to (group.name ?: ServerBot.config.integration.group.settings.nullPlaceholder)
        )

    fun escapeName(current: String) =
        current.replace(" ", "_").replace(Regex("[^а-яa-z0-9_\\-]", RegexOption.IGNORE_CASE), "")
    suspend fun resolveFeaturesSettingsMessage(group: SQLGroup) =
        ServerBot.config.integration.group.groupHasNoOnlyPlayers.let { if (!group.hasProtectedLevel(AccountType.PLAYER)) it else "" } +
        "\n" + ServerBot.config.integration.group.selectFeature

    open class GroupCallback(
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
        SELECT_FEATURE,
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
        @SerializedName("remove_agree_with_rules")
        REMOVE_AGREE_WITH_RULES,
        @SerializedName("confirm_remove_agree_with_rules")
        CONFIRM_REMOVE_AGREE_WITH_RULES,
        @SerializedName("cancel_remove_agree_with_rules")
        CANCEL_REMOVE_AGREE_WITH_RULES,
        @SerializedName("setup_feature")
        SETUP_FEATURE,
        @SerializedName("success")
        SUCCESS,
    }
    data class SetupFeatureCallback<T: FeatureData>(
        val feature: FeatureType<T>,
        val temp: T,
        val field: String,
        val arg: String,
    )
    class FeatureGroupCallback<T: FeatureData>(
        val data: SetupFeatureCallback<T>,
        name: String? = null,
    ): GroupCallback(Operations.SETUP_FEATURE, name)
}