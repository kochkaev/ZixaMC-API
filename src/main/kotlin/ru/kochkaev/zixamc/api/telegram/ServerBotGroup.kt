package ru.kochkaev.zixamc.api.telegram

import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.ConfigManager
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
import ru.kochkaev.zixamc.api.sql.process.GroupSelectTopicProcessData
import ru.kochkaev.zixamc.api.sql.process.GroupWaitingNameProcessData
import ru.kochkaev.zixamc.api.sql.process.ProcessTypes
import ru.kochkaev.zixamc.api.telegram.model.ITgMenuButton
import ru.kochkaev.zixamc.api.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.api.telegram.model.TgChatMemberStatuses
import ru.kochkaev.zixamc.api.telegram.model.TgChatType
import ru.kochkaev.zixamc.api.telegram.model.TgMessage
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters
import kotlin.collections.contains

object ServerBotGroup {

    val CAN_EXECUTE_ADMIN = CallbackCanExecute(
        statuses = listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR),
        display = ServerBot.config.group.memberStatus.administrators,
    )
    val CAN_EXECUTE_OWNER = CallbackCanExecute(
        statuses = listOf(TgChatMemberStatuses.CREATOR),
        display = ServerBot.config.group.memberStatus.creator,
    )
    fun getSettings(group: SQLGroup) = TgMenu(
        arrayListOf<List<ITgMenuButton>>(
            listOf(
                SQLCallback.of(
                    display = ServerBot.config.group.settings.features,
                    type = "group",
                    data = GroupCallback.of(Operations.EDIT_FEATURES),
                    canExecute = CAN_EXECUTE_ADMIN,
                )
            ),
            listOf(
                SQLCallback.of(
                    display = ServerBot.config.group.settings.changeName,
                    type = "group",
                    data = GroupCallback.of(Operations.UPDATE_NAME),
                    canExecute = CAN_EXECUTE_ADMIN,
                )
            ),
            listOf(
                SQLCallback.of(
                    display = ServerBot.config.group.settings.aliases,
                    type = "group",
                    data = GroupCallback.of(Operations.GET_ALIASES),
                    canExecute = CAN_EXECUTE_ADMIN,
                )
            ),
        ).apply {
            addAll(settingsIntegrations.filter { it.filter(group) } .map { it.button })
            add(listOf(
                SQLCallback.of(
                    display = ServerBot.config.group.settings.removeAgreedWithRules,
                    type = "rules",
                    data = RulesManager.RulesCallbackData(
                        operation = RulesManager.RulesOperation.REMOVE_AGREE_GROUP,
                        type = RulesManager.RulesOperationType.REMOVE_AGREE,
                    ),
                    canExecute = CAN_EXECUTE_ADMIN,
                )
            ))
            add(listOf(
                SQLCallback.of(
                    display = ConfigManager.config.general.buttons.success,
                    type = "group",
                    data = GroupCallback.of(Operations.SUCCESS),
                    canExecute = CAN_EXECUTE_ADMIN,
                )
            ))
        }
    )
    suspend fun backToSettings(group: SQLGroup, messageId: Int) {
        ServerBot.bot.editMessageText(
            chatId = group.id,
            messageId = messageId,
            text = getSettingsText(group),
        )
        ServerBot.bot.editMessageReplyMarkup(
            chatId = group.id,
            messageId = messageId,
            replyMarkup = getSettings(group),
        )
    }
    suspend fun sendSettings(group: SQLGroup, replyTo: Int? = null) {
        ServerBot.bot.sendMessage(
            chatId = group.id,
            text = getSettingsText(group),
            replyMarkup = getSettings(group),
            replyParameters = replyTo?.let { TgReplyParameters(it) }
        )
    }

    private val settingsIntegrations = arrayListOf<Integration>()
    fun registerSettingsIntegration(integration: Integration) {
        settingsIntegrations.add(integration)
    }
    data class Integration(
        val button: List<ITgMenuButton>,
        val callbackProcessor: suspend (TgCallbackQuery, SQLCallback<GroupCallback<*>>) -> TgCBHandlerResult,
        val filter: (SQLGroup) -> Boolean = { group -> true },
    ) {
        companion object {
            fun of(
                button: ITgMenuButton,
                filter: (SQLGroup) -> Boolean = { group -> true },
            ): Integration = Integration(listOf(button), { _, _ -> TgCBHandlerResult.SUCCESS }, filter)
            fun of(
                callbackName: String,
                menuDisplay: String,
                processor: suspend (TgCallbackQuery, SQLCallback<GroupCallback<GroupCallback.DummyAdditional>>) -> TgCBHandlerResult,
                filter: (SQLGroup) -> Boolean = { group -> true },
                canExecute: CallbackCanExecute = CAN_EXECUTE_ADMIN
            ): Integration = of(callbackName, menuDisplay, processor, GroupCallback.DummyAdditional::class.java, GroupCallback.DummyAdditional(), filter, canExecute)
            fun <T> of(
                callbackName: String,
                menuDisplay: String,
                processor: suspend (TgCallbackQuery, SQLCallback<GroupCallback<T>>) -> TgCBHandlerResult,
                customDataType: Class<T>,
                customDataInitial: T,
                filter: (SQLGroup) -> Boolean = { group -> true },
                canExecute: CallbackCanExecute = CAN_EXECUTE_ADMIN
            ): Integration {
                return Integration(
                    button = listOf(
                        SQLCallback.of(
                            display = menuDisplay,
                            type = "group",
                            data = GroupCallback.of(callbackName, customDataType, customDataInitial),
                            canExecute = canExecute,
                        )),
                    callbackProcessor = { cbq, sql ->
                        if (sql.data?.operation == callbackName)
                            @Suppress("UNCHECKED_CAST")
                            processor(cbq, sql as SQLCallback<GroupCallback<T>>)
                        else TgCBHandlerResult.SUCCESS
                    },
                    filter = filter,
                )
            }
        }
    }

    suspend fun newChatMembers(msg: TgMessage) {
        if (msg.chat.type == TgChatType.CHANNEL) {
            ServerBot.bot.leaveChat(msg.chat.id)
        }
        val group = SQLGroup.getOrCreate(msg.chat.id)
        msg.newChatMembers!!.forEach { member ->
            group.members.add(member.id)
            if (member.id == ServerBot.bot.me.id && msg.from != null) {
                try {
                    group.deleteProtected(AccountType.UNKNOWN)
                } catch (_: Exception) {}
                SQLCallback.getAll(group.id).sortedByDescending { it.messageId }.forEach {
                    it.messageId?.also { messageId -> try {
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.id,
                            messageId = messageId,
                            replyMarkup = TgReplyMarkup()
                        )
                    } catch (_: Exception) { } }
                    it.drop()
                }
                val user = SQLUser.get(msg.from.id)
                if (user == null || !user.hasProtectedLevel(AccountType.PLAYER)) {
                    ServerBot.bot.sendMessage(
                        chatId = msg.chat.id,
                        text = ServerBot.config.group.sorryOnlyForPlayer,
                    )
                    ServerBot.bot.leaveChat(msg.chat.id)
                    return
                }
                else if (group.isRestricted) {
                    ServerBot.bot.sendMessage(
                        chatId = msg.chat.id,
                        text = ServerBot.config.group.restrict,
                    )
                    ServerBot.bot.leaveChat(msg.chat.id)
                    return
                }
                ServerBot.bot.sendMessage(
                    chatId = msg.chat.id,
                    text = ServerBot.config.group.hello
                )
                if (!group.agreedWithRules) {
                    ServerBot.bot.sendMessage(
                        chatId = msg.chat.id,
                        text = ServerBot.config.group.needAgreeWithRules,
                        replyMarkup = TgMenu(
                            listOf(
                                listOf(
                                    SQLCallback.of(
                                        display = ConfigManager.config.general.rules.agreeButton,
                                        type = "group",
                                        data = GroupCallback.of(Operations.AGREE_WITH_RULES),
                                        canExecute = CAN_EXECUTE_OWNER,
                                    )
                                )
                            )
                        )
                    )
                }
                else sendFeatures(group)
            }
            else if (!member.isBot) {
                val user = SQLUser.getOrCreate(member.id)
                user.accountType.levelHigh?.let { group.deleteProtected(it) }
                if (group.data.getCasted(ChatDataTypes.GREETING_ENABLE)?:false) ServerBot.bot.sendMessage(
                    chatId = group.id,
                    text = ServerBot.config.group.protectedWasDeleted
                )
            }
        }
    }
    suspend fun newChatMembersRequests(msg: TgMessage) {
        val user = msg.from?.let { SQLUser.get(it.id) } ?: return
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

    suspend fun onCallback(cbq: TgCallbackQuery, sql: SQLCallback<GroupCallback<*>>): TgCBHandlerResult {
        if (sql.data == null) return TgCBHandlerResult.Companion.SUCCESS
        val group = SQLGroup.Companion.get(cbq.message.chat.id) ?: return TgCBHandlerResult.Companion.SUCCESS
        when (Operations.deserialize(sql.data!!.operation)) {
            Operations.AGREE_WITH_RULES -> {
                group.agreedWithRules = true
                if (group.name == null) {
                    val message = ServerBot.bot.sendMessage(
                        chatId = group.id,
                        text = ServerBot.config.group.thinkOfName,
                        replyMarkup = TgMenu(
                            listOfNotNull(
                                cbq.message.chat.title?.let { escapeName(it) }?.let {
                                    if (group.canTakeName(it)) listOf(
                                        SQLCallback.Companion.of(
                                            display = it,
                                            type = "group",
                                            data = GroupCallback.of(
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
                                            data = GroupCallback.of(
                                                operation = Operations.SET_NAME,
                                                name = it
                                            ),
                                            canExecute = CAN_EXECUTE_ADMIN,
                                        )
                                    ) else listOf()
                                },
                            ))
                    )
                    SQLProcess.Companion.get(group.id, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                        this.data?.messageId?.also { try {
                            ServerBot.bot.editMessageReplyMarkup(
                                chatId = group.id,
                                messageId = it,
                                replyMarkup = TgReplyMarkup()
                            )
                            SQLCallback.Companion.dropAll(group.id, it)
                        } catch (_: Exception) {} }
                    } ?.drop()
                    SQLProcess.Companion.of(
                        ProcessTypes.GROUP_WAITING_NAME, GroupWaitingNameProcessData(
                            messageId = message.messageId,
                            nameType = GroupWaitingNameProcessData.NameType.NAME,
                        )
                    ).pull(group.id)
                }
                else sendFeatures(group, cbq.message.messageId, true, null)
//                } else {
//                    bot.answerCallbackQuery(
//                        callbackQueryId = cbq.id,
//                        text = config.group.needAgreeOwner,
//                        showAlert = true
//                    )
//                    return SUCCESS
//                }
            }
            Operations.SET_NAME -> {
                group.name = (sql.data!!.additional as? GroupCallback.AdditionalWithName)?.name
                SQLProcess.Companion.get(group.id, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                    this.data?.messageId?.also { try {
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.id,
                            messageId = it,
                            replyMarkup = TgReplyMarkup()
                        )
                        SQLCallback.Companion.dropAll(group.id, it)
                    } catch (_: Exception) {} }
                } ?.drop()
                sendFeatures(group, cbq.message.messageId, true, null)
                return TgCBHandlerResult.SUCCESS
            }
            Operations.UPDATE_NAME -> {
                SQLProcess.Companion.get(group.id, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                    this.data?.messageId?.also { try {
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.id,
                            messageId = it,
                            replyMarkup = TgReplyMarkup()
                        )
                        SQLCallback.Companion.dropAll(group.id, it)
                    } catch (_: Exception) {} }
                } ?.drop()
                SQLProcess.Companion.of(
                    ProcessTypes.GROUP_WAITING_NAME, GroupWaitingNameProcessData(
                        messageId = cbq.message.messageId,
                        nameType = GroupWaitingNameProcessData.NameType.NAME
                    )
                ).pull(group.id)
                ServerBot.bot.editMessageText(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.group.thinkOfName,
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                CancelCallbackData(
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback.of(Operations.SETTINGS),
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
                SQLProcess.Companion.get(group.id, ProcessTypes.GROUP_WAITING_NAME)?.apply {
                    this.data?.messageId?.also { try {
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.id,
                            messageId = it,
                            replyMarkup = TgReplyMarkup()
                        )
                        SQLCallback.Companion.dropAll(group.id, it)
                    } catch (_: Exception) {} }
                } ?.drop()
                SQLProcess.Companion.of(
                    ProcessTypes.GROUP_WAITING_NAME, GroupWaitingNameProcessData(
                        messageId = cbq.message.messageId,
                        nameType = GroupWaitingNameProcessData.NameType.ALIAS
                    )
                ).pull(group.id)
                ServerBot.bot.editMessageText(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.group.thinkOfName,
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                CancelCallbackData(
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback.of(Operations.GET_ALIASES),
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
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.group.settings.aliasesDescription,
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        (group.aliases.get()?.let {
                            it.fold(arrayListOf<List<SQLCallback.Companion.Builder<out CallbackData>>>()) { acc, a ->
                                acc.add(
                                    listOf(
                                        SQLCallback.Companion.of(
                                            display = ServerBot.config.group.settings.removeAlias.formatLang(
                                                "alias" to a
                                            ),
                                            type = "group",
                                            data = GroupCallback.of(
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
                                        display = ServerBot.config.group.settings.addAlias,
                                        type = "group",
                                        data = GroupCallback.of(Operations.ADD_ALIAS),
                                        canExecute = CAN_EXECUTE_ADMIN,
                                    )
                                )
                            )
                            this.add(
                                listOf(
                                    CancelCallbackData(
                                        asCallbackSend = CancelCallbackData.CallbackSend(
                                            type = "group",
                                            data = GroupCallback.of(Operations.SETTINGS),
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
                if ((sql.data!!.additional as? GroupCallback.AdditionalWithName)?.name == null) return TgCBHandlerResult.Companion.SUCCESS
                val name = (sql.data!!.additional as GroupCallback.AdditionalWithName).name!!
                group.aliases.remove(name)
                ServerBot.bot.editMessageText(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.group.settings.aliasDeleted.formatLang(
                        "alias" to name
                    ),
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                CancelCallbackData(
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback.of(Operations.GET_ALIASES),
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
                val name = (sql.data!!.additional as? GroupCallback.AdditionalWithName)?.name
                val feature = FeatureTypes.entries[name] ?: return TgCBHandlerResult.Companion.DELETE_MARKUP
                return feature.setUp(cbq, group)
            }
            Operations.SEND_FEATURES -> {
                sendFeatures(group, null, false, cbq.message.messageId)
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.EDIT_FEATURES -> {
                ServerBot.bot.editMessageText(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.group.settings.featuresDescription,
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        (group.features.getAll()?.let {
                            it.keys.fold(arrayListOf<List<SQLCallback.Companion.Builder<out CallbackData>>>()) { acc, feature ->
                                acc.add(
                                    listOf(
                                        SQLCallback.Companion.of(
                                            display = feature.tgDisplayName(),
                                            type = "group",
                                            data = GroupCallback.of(
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
                                        display = ServerBot.config.group.settings.addFeature,
                                        type = "group",
                                        data = GroupCallback.of(Operations.SEND_FEATURES),
                                        canExecute = CAN_EXECUTE_ADMIN,
                                    )
                                )
                            )
                            this.add(
                                listOf(
                                    CancelCallbackData(
                                        asCallbackSend = CancelCallbackData.CallbackSend(
                                            type = "group",
                                            data = GroupCallback.of(Operations.SETTINGS),
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
                val name = (sql.data!!.additional as? GroupCallback.AdditionalWithName)?.name ?: TgCBHandlerResult.Companion.SUCCESS
                val type = FeatureTypes.entries[name] ?: return TgCBHandlerResult.Companion.SUCCESS
                val data = group.features.getCasted(type) ?: return TgCBHandlerResult.Companion.SUCCESS
                ServerBot.bot.editMessageText(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.group.settings.featureDescription.formatLang(
                        "feature" to type.tgDisplayName(),
                        "options" to type.getResolvedOptions(data),
                    ),
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                SQLCallback.Companion.of(
                                    display = ServerBot.config.group.settings.editFeature,
                                    type = "group",
                                    data = GroupCallback.of(
                                        operation = Operations.EDIT_FEATURE_DATA,
                                        name = type.serializedName,
                                    ),
                                    canExecute = CAN_EXECUTE_ADMIN,
                                )
                            ),
                            listOf(
                                SQLCallback.Companion.of(
                                    display = ServerBot.config.group.settings.removeFeature,
                                    type = "group",
                                    data = GroupCallback.of(
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
                                        data = GroupCallback.of(Operations.EDIT_FEATURES),
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
                val name = (sql.data!!.additional as? GroupCallback.AdditionalWithName)?.name ?: TgCBHandlerResult.Companion.SUCCESS
                val type = FeatureTypes.entries[name] ?: return TgCBHandlerResult.Companion.SUCCESS
                return type.sendEditor(cbq, group)
            }
            Operations.CONFIRM_SETUP_FEATURE -> {
                val name = (sql.data!!.additional as? GroupCallback.AdditionalWithName)?.name ?: TgCBHandlerResult.Companion.SUCCESS
                val type = FeatureTypes.entries[name] ?: return TgCBHandlerResult.Companion.SUCCESS
                return type.finishSetUp(group, cbq.message.messageId)
            }
            Operations.REMOVE_FEATURE -> {
                val name = (sql.data!!.additional as? GroupCallback.AdditionalWithName)?.name ?: TgCBHandlerResult.Companion.SUCCESS
                val type = FeatureTypes.entries[name] ?: return TgCBHandlerResult.Companion.SUCCESS
                group.features.remove(type)
                ServerBot.bot.editMessageText(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.group.settings.featureRemoved.formatLang(
                        "feature" to type.tgDisplayName()
                    ),
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                CancelCallbackData(
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback.of(Operations.EDIT_FEATURES),
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
                backToSettings(group, cbq.message.messageId)
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            Operations.TOPIC_RESELECT -> {
                val name = (sql.data!!.additional as? GroupCallback.AdditionalWithName)?.name ?: TgCBHandlerResult.Companion.SUCCESS
                val type = FeatureTypes.entries[name] ?: return TgCBHandlerResult.Companion.SUCCESS
                ServerBot.bot.editMessageText(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    text = ServerBot.config.group.selectTopicForFeature
                )
                SQLProcess.Companion.get(group.id, ProcessTypes.GROUP_SELECT_TOPIC_FEATURE)?.run {
                    this.data?.also { ServerBot.bot.deleteMessage(group.id, it.messageId) }
                    this.drop()
                }
                SQLProcess.Companion.of(
                    ProcessTypes.GROUP_SELECT_TOPIC_FEATURE,
                    GroupSelectTopicProcessData(cbq.message.messageId, type.serializedName)
                ).pull(group.id)
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                CancelCallbackData(
                                    cancelProcesses = listOf(ProcessTypes.GROUP_SELECT_TOPIC_FEATURE),
                                    asCallbackSend = CancelCallbackData.CallbackSend(
                                        type = "group",
                                        data = GroupCallback.of(
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
            Operations.SETUP_FEATURE -> {
                val data = sql.data!!
                return (data.additional as? SetupFeatureCallback<out FeatureData>)?.feature?.uncheckedProcessSetup(cbq, group, data) ?: TgCBHandlerResult.Companion.SUCCESS
            }
            Operations.SUCCESS -> {
                ServerBot.bot.deleteMessage(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                )
                return TgCBHandlerResult.Companion.DELETE_LINKED
            }
            null -> {
                settingsIntegrations.forEach { integration ->
                    val result = integration.callbackProcessor(cbq, sql)
                    if (result != TgCBHandlerResult.Companion.SUCCESS) return result
                }
            }
        }
        return TgCBHandlerResult.Companion.DELETE_MARKUP
    }

    suspend fun waitNameProcessor(msg: TgMessage, process: SQLProcess<GroupWaitingNameProcessData>, data: GroupWaitingNameProcessData) {
        val group = SQLGroup.Companion.get(msg.chat.id) ?: return
        if (msg.replyToMessage?.from?.id == ServerBot.bot.me.id && msg.from != null &&
            listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(
                ServerBot.bot.getChatMember(group.id, msg.from.id).status
            )
        ) {
            val inFirstTime = group.name == null
//            val data = processData as GroupWaitingNameProcessData
            if (data.messageId != msg.replyToMessage.messageId) return
            val name = msg.effectiveText ?: return
            if (name.length !in 1..16 || !name.matches(Regex("[а-яa-z0-9_\\-]+", RegexOption.IGNORE_CASE))) {
                ServerBot.bot.sendMessage(
                    chatId = group.id,
                    text = ServerBot.config.group.incorrectName,
                    replyParameters = TgReplyParameters(
                        msg.messageId
                    )
                )
            } else if (!group.canTakeName(name)) {
                ServerBot.bot.sendMessage(
                    chatId = group.id,
                    text = ServerBot.config.group.nameIsTaken,
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
                    chatId = group.id,
                    messageId = data.messageId,
                    replyMarkup = TgReplyMarkup(),
                )
                SQLCallback.Companion.dropAll(group.id, data.messageId)
                process.drop()
                if (inFirstTime) sendFeatures(group, msg.messageId, true, null)
                else settingsCommand(msg)
            }
        }

    }

    suspend fun answerHaventRights(id: String, display: String, bot: TelegramBotZixa): TgCBHandlerResult {
        bot.answerCallbackQuery(
            callbackQueryId = id,
            text = ServerBot.config.group.haveNotPermission.formatLang(
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
                ServerBot.bot.getChatMember(group.id, msg.from.id).status
            )
        )) return
        val processes = SQLProcess.Companion.getAll(group.id)
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
                            chatId = group.id,
                            text = getSettingsText(group),
                            replyMarkup = getSettings(group),
                            replyParameters = TgReplyParameters(
                                msg.messageId
                            ),
                        )
                    }
                    else feature.finishSetUp(group, topicId, topicId)
                    try {
                        SQLCallback.Companion.getAll(group.id, data.messageId).forEach { it.drop() }
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.id,
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
                ServerBot.bot.getChatMember(group.id, msg.from.id).status
            )
        ) return
        sendSettings(group, msg.messageId)
    }

    suspend fun sendFeatures(group: SQLGroup, replyTo: Int? = null, withDone: Boolean = false, edit: Int? = null) {
        val message = (if (withDone) ServerBot.config.group.done + "\n" else "") + resolveFeaturesSettingsMessage(group)
        val menu = TgMenu(
            FeatureTypes.entries
                .map { it.value }
                .filter { it.checkAvailable(group) }
                .filter { !group.features.contains(it) }
                .map<FeatureType<*>, SQLCallback.Companion.Builder<*>> {
                    SQLCallback.of(
                        display = it.tgDisplayName(),
                        type = "group",
                        data = GroupCallback.of(
                            operation = Operations.SELECT_FEATURE,
                            name = it.serializedName
                        ),
                        canExecute = CAN_EXECUTE_ADMIN,
                    )
                }
                .toMutableList()
                .apply {
                    if (withDone) add(SQLCallback.of(
                        display = ConfigManager.config.general.buttons.success,
                        type = "group",
                        data = GroupCallback.of(Operations.SUCCESS),
                        canExecute = CAN_EXECUTE_ADMIN,
                    ))
                    else add(CancelCallbackData(
                        asCallbackSend = CancelCallbackData.CallbackSend(
                            type = "group",
                            data = GroupCallback.of(Operations.EDIT_FEATURES),
                            result = TgCBHandlerResult.DELETE_LINKED,
                        )
                    ).build())
                }
                .map { listOf(it) }
        )
        if (edit == null) ServerBot.bot.sendMessage(
            chatId = group.id,
            text = message,
            replyParameters = replyTo?.let { TgReplyParameters(it) },
            replyMarkup = menu
        )
        else {
            ServerBot.bot.editMessageText(
                chatId = group.id,
                messageId = edit,
                text = message,
            )
            ServerBot.bot.editMessageReplyMarkup(
                chatId = group.id,
                messageId = edit,
                replyMarkup = menu
            )
        }
    }
    fun getSettingsText(group: SQLGroup) =
        ServerBot.config.group.settings.text.formatLang(
            "groupName" to (group.name ?: ServerBot.config.group.settings.nullPlaceholder)
        )

    fun escapeName(current: String) =
        current.replace(" ", "_").replace(Regex("[^а-яa-z0-9_\\-]", RegexOption.IGNORE_CASE), "")
    suspend fun resolveFeaturesSettingsMessage(group: SQLGroup) =
        ServerBot.config.group.groupHasNoOnlyPlayers.let { if (!group.hasProtectedLevel(AccountType.PLAYER)) it else "" } +
        "\n" + ServerBot.config.group.selectFeature

    val integrationTypes = hashMapOf<String, Class<*>>(
        "dummy" to GroupCallback.DummyAdditional::class.java,
        "withName" to GroupCallback.AdditionalWithName::class.java,
    )
    open class GroupCallback<T> private constructor(
        var operation: String,
        val additionalType: String,
        val additional: T,
    ): CallbackData {
        open class DummyAdditional()
        open class AdditionalWithName(
            val name: String? = null
        )
        companion object {
            fun of(operation: String): GroupCallback<DummyAdditional> =
                GroupCallback(operation, "dummy", DummyAdditional())
            fun of(operation: Operations): GroupCallback<DummyAdditional> =
                GroupCallback(operation.serialized(), "dummy", DummyAdditional())
            fun of(operation: String, name: String?): GroupCallback<AdditionalWithName> =
                GroupCallback(operation, "withName", AdditionalWithName(name))
            fun of(operation: Operations, name: String?): GroupCallback<AdditionalWithName> =
                GroupCallback(operation.serialized(), "withName", AdditionalWithName(name))
            fun <T> of(operation: String, additionalType: Class<T>, additional: T): GroupCallback<T> {
                val type = additionalType.name
                if (!integrationTypes.contains(type))
                    integrationTypes[type] = additionalType
                return GroupCallback(operation, type, additional)
            }
            fun <T> of(operation: Operations, additionalType: Class<T>, additional: T): GroupCallback<T> {
                return of(operation.serialized(), additionalType, additional)
            }
        }
    }
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
        @SerializedName("confirm_setup_feature")
        CONFIRM_SETUP_FEATURE,
        @SerializedName("setup_feature")
        SETUP_FEATURE,
        @SerializedName("success")
        SUCCESS;

        open fun serialized(): String = this.name.lowercase()
        companion object {
            fun deserialize(name: String): Operations? = entries.firstOrNull { it.serialized() == name }
        }
    }
    data class SetupFeatureCallback<T: FeatureData>(
        val feature: FeatureType<T>,
        val temp: T,
        val field: String,
        val arg: String,
    )
}