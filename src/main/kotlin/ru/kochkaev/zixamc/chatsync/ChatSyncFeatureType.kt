package ru.kochkaev.zixamc.chatsync

import ru.kochkaev.zixamc.api.config.TextData
import ru.kochkaev.zixamc.api.escapeHTML
import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup.GroupCallback
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup.Operations
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLProcess
import ru.kochkaev.zixamc.api.sql.callback.CallbackData
import ru.kochkaev.zixamc.api.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult.Companion.DELETE_LINKED
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.chatsync.GroupChatSyncWaitPrefixProcessData
import ru.kochkaev.zixamc.api.sql.process.ProcessTypes
import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.api.telegram.ServerBot.config
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup.SETTINGS
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup.getSettingsText
import ru.kochkaev.zixamc.api.sql.feature.TopicFeatureType
import ru.kochkaev.zixamc.api.telegram.model.*

object ChatSyncFeatureType: TopicFeatureType<ChatSyncFeatureData>(
    model = ChatSyncFeatureData::class.java,
    serializedName = "CHAT_SYNC",
    tgDisplayName = { config.integration.group.features.chatSync.display },
    tgDescription = { config.integration.group.features.chatSync.description },
    tgOnDone = {
        if (bot.getChat(it.chatId).isForum)
            config.integration.group.features.chatSync.doneTopic
        else config.integration.group.features.chatSync.doneNoTopic
    },
    checkAvailable = { true },
    getDefault = { ChatSyncFeatureData() },
    optionsResolver = {
        config.integration.group.features.chatSync.options.formatLang(
            "topicId" to (it.topicId?.toString() ?: config.integration.group.settings.nullTopicPlaceholder),
            "prefix" to (it.prefix?.raw?.escapeHTML() ?: config.integration.group.settings.nullPlaceholder),
            "fromMcPrefix" to (it.fromMcPrefix?.raw?.escapeHTML() ?: config.integration.group.settings.nullPlaceholder)
        )
    }
) {
    override suspend fun finishSetUp(group: SQLGroup, replyTo: Int?, topicId: Int?) {
        sendNeedPrefix(group, replyTo, topicId, GroupChatSyncWaitPrefixProcessData.PrefixTypes.DEFAULT)
    }
    suspend fun sendNeedPrefix(group: SQLGroup, replyTo: Int?, topicId: Int?, prefixType: GroupChatSyncWaitPrefixProcessData.PrefixTypes) {
        val isNotNew = group.features.contains(this)
        val menu = TgMenu(listOf(listOf(
            CancelCallbackData(
                cancelProcesses = listOf(ProcessTypes.GROUP_SELECT_TOPIC_FEATURE),
                asCallbackSend = CancelCallbackData.CallbackSend(
                    type = "group",
                    data = GroupCallback(
                        operation = if (isNotNew) Operations.EDIT_FEATURE_DATA else Operations.SEND_FEATURES,
                        name = if (isNotNew) serializedName else null,
                    ),
                    result = DELETE_LINKED,
                )
            ).build()
        )))
        val message = bot.sendMessage(
            chatId = group.chatId,
            text = config.integration.group.features.chatSync.prefixNeeded.formatLang(
                "groupName" to group.name.toString(),
            ),
            replyParameters = replyTo?.let { TgReplyParameters(it) },
            replyMarkup = menu
        )
        SQLProcess.get(group.chatId, ChatSyncWaitingPrefixProcess)?.apply {
            this.data?.messageId?.also {
                try { bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = it,
                    replyMarkup = TgReplyMarkup()
                ) } catch (_: Exception) {}
                SQLCallback.dropAll(group.chatId, it)
            }
        } ?.drop()
        SQLProcess.of(ChatSyncWaitingPrefixProcess, GroupChatSyncWaitPrefixProcessData(topicId, message.messageId, prefixType)).pull(group.chatId)
    }

    override fun getEditorMarkup(cbq: TgCallbackQuery, group: SQLGroup): ArrayList<List<SQLCallback.Companion.Builder<out CallbackData>>> {
        val origin = super.getEditorMarkup(cbq, group)
        val isForum = cbq.message.chat.isForum
        origin.add(if (isForum) 1 else 0, listOf(
            SQLCallback.of(
            display = config.integration.group.features.chatSync.editPrefix,
            type = "group",
            data = GroupCallback(
                operation = Operations.EDIT_PREFIX,
                name = GroupChatSyncWaitPrefixProcessData.PrefixTypes.DEFAULT.name,
            )
        )))
        origin.add(if (isForum) 2 else 1, listOf(
            SQLCallback.of(
            display = config.integration.group.features.chatSync.editPrefixMC,
            type = "group",
            data = GroupCallback(
                operation = Operations.EDIT_PREFIX,
                name = GroupChatSyncWaitPrefixProcessData.PrefixTypes.FROM_MINECRAFT.name,
            )
        )))
        return origin
    }

    suspend fun waitPrefixProcessor(msg: TgMessage, process: SQLProcess<GroupChatSyncWaitPrefixProcessData>, data: GroupChatSyncWaitPrefixProcessData) {
        val group = SQLGroup.get(msg.chat.id) ?: return
        if (msg.replyToMessage?.from?.id == bot.me.id && msg.from != null &&
            listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(
                bot.getChatMember(group.chatId, msg.from.id).status
            )
        ) {
//            val data = processData as GroupChatSyncWaitPrefixProcessData
            if (data.messageId != msg.replyToMessage.messageId) return
            val isNotNew = group.features.contains(ChatSyncFeatureType)
            val prefix = msg.effectiveText?:return
            val mm = TextData(prefix)
            try {
                mm.get()
            } catch (e: Exception) {
                bot.sendMessage(
                    chatId = group.chatId,
                    text = config.integration.group.features.chatSync.wrongPrefix.formatLang(
                        "error" to e.message.toString()
                    )
                )
                return
            }
            group.features.set(
                ChatSyncFeatureType,
                if (!isNotNew)
                    getDefault(group).apply {
                        this.topicId = data.topicId
                        this.prefix = mm
                    }
                else group.features.getCasted(ChatSyncFeatureType)!!.apply {
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
                    replyParameters = TgReplyParameters(msg.messageId)
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