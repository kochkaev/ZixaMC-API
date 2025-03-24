package ru.kochkaev.zixamc.tgbridge.telegram.feature.type

import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.GroupCallback
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.Operations
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.sql.SQLProcess
import ru.kochkaev.zixamc.tgbridge.sql.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult.Companion.DELETE_MARKUP
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgMenu
import ru.kochkaev.zixamc.tgbridge.sql.process.GroupChatSyncWaitPrefixProcessData
import ru.kochkaev.zixamc.tgbridge.sql.process.ProcessTypes
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.telegram.feature.TopicFeatureType
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.ChatSyncTopicData
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyMarkup

object ChatSyncFeatureType: TopicFeatureType<ChatSyncTopicData>(
    model = ChatSyncTopicData::class.java,
    serializedName = "CHAT_SYNC",
    tgDisplayName = { config.integration.group.features.chatSync.display },
    tgDescription = { config.integration.group.features.chatSync.description },tgOnDone = {
        if (bot.getChat(it.chatId).isForum)
            config.integration.group.features.chatSync.doneTopic
        else config.integration.group.features.chatSync.doneNoTopic
    },
    checkAvailable = { true },
    getDefault = { ChatSyncTopicData() },
    optionsResolver = {
        TextParser.formatLang(
            text = config.integration.group.features.chatSync.options,
            "topicId" to (it.topicId?.toString() ?: config.integration.group.settings.nullTopicPlaceholder),
            "prefix" to (it.prefix?.raw?.let { it1 -> TextParser.escapeHTML(it1) } ?: config.integration.group.settings.nullPlaceholder),
            "fromMcPrefix" to (it.fromMcPrefix?.raw?.let { it1 -> TextParser.escapeHTML(it1) } ?: config.integration.group.settings.nullPlaceholder)
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
                    result = DELETE_MARKUP,
                )
            ).build()
        )))
        val message = bot.sendMessage(
            chatId = group.chatId,
            text = TextParser.formatLang(
                text = config.integration.group.features.chatSync.prefixNeeded,
                "groupName" to group.name.toString(),
            ),
            replyParameters = replyTo?.let { ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyParameters(it) },
            replyMarkup = menu
        )
        SQLProcess.get(group.chatId, ProcessTypes.GROUP_CHATSYNC_WAITING_PREFIX)?.apply {
            this.data?.messageId?.also {
                try { bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = it,
                    replyMarkup = TgReplyMarkup()
                ) } catch (_: Exception) {}
                SQLCallback.dropAll(group.chatId, it)
            }
        } ?.drop()
        SQLProcess.of(ProcessTypes.GROUP_CHATSYNC_WAITING_PREFIX, GroupChatSyncWaitPrefixProcessData(topicId, message.messageId, prefixType)).pull(group.chatId)
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
}