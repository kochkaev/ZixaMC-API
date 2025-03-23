package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgCallbackQuery
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.*
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCBHandlerResult.Companion.DELETE_MARKUP
import ru.kochkaev.zixamc.tgbridge.serverBot.group.ServerBotGroupUpdateManager.GroupCallback
import ru.kochkaev.zixamc.tgbridge.serverBot.group.ServerBotGroupUpdateManager.Operations
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.sql.SQLProcess

object FeatureTypes {
    val CHAT_SYNC: TopicFeatureType<ChatSyncTopicData> = object: TopicFeatureType<ChatSyncTopicData>(
        model = ChatSyncTopicData::class.java,
        serializedName = "CHAT_SYNC",
        tgDisplayName = { config.integration.group.features.chatSync.display },
        tgDescription = { config.integration.group.features.chatSync.description },
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
    ), IChatSyncType {
        override suspend fun finishSetUp(group: SQLGroup, replyTo: Int?, topicId: Int?) {
            sendNeedPrefix(group, replyTo, topicId, GroupChatSyncWaitPrefixProcessData.PrefixTypes.DEFAULT)
        }
        override suspend fun sendNeedPrefix(group: SQLGroup, replyTo: Int?, topicId: Int?, prefixType: GroupChatSyncWaitPrefixProcessData.PrefixTypes) {
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
            ))).inlineAndId()
            val message = bot.sendMessage(
                chatId = group.chatId,
                text = TextParser.formatLang(
                    text = config.integration.group.features.chatSync.prefixNeeded,
                    "groupName" to group.name.toString(),
                ),
                replyParameters = replyTo?.let { TgReplyParameters(it) },
                replyMarkup = menu.first
            )
            SQLProcess.get(group.chatId, ProcessTypes.GROUP_CHATSYNC_WAITING_PREFIX)?.apply {
                this.data?.callbacksToDelete?.forEach { SQLCallback.get(it)?.drop() }
                this.data?.messageId?.also {
                    try { bot.editMessageReplyMarkup(
                        chatId = group.chatId,
                        messageId = it,
                        replyMarkup = TgReplyMarkup()
                    ) } catch (_: Exception) {}
                }
            } ?.drop()
            SQLProcess.of(ProcessTypes.GROUP_CHATSYNC_WAITING_PREFIX, GroupChatSyncWaitPrefixProcessData(topicId, message.messageId, prefixType, menu.second)).pull(group.chatId)
        }

        override fun getEditorMarkup(cbq: TgCallbackQuery, group: SQLGroup): ArrayList<List<SQLCallback.Companion.Builder<out CallbackData>>> {
            val origin = super.getEditorMarkup(cbq, group)
            val isForum = cbq.message.chat.isForum
            origin.add(if (isForum) 1 else 0, listOf(SQLCallback.of(
                display = config.integration.group.features.chatSync.editPrefix,
                type = "group",
                data = GroupCallback(
                    operation = Operations.EDIT_PREFIX,
                    name = GroupChatSyncWaitPrefixProcessData.PrefixTypes.DEFAULT.name,
                )
            )))
            origin.add(if (isForum) 2 else 1, listOf(SQLCallback.of(
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
    val CONSOLE = FeatureType(
        model = ChatSyncTopicData::class.java,
        serializedName = "CONSOLE",
        tgDisplayName = { config.integration.group.features.chatSync.display },
        tgDescription = { config.integration.group.features.chatSync.description },
        checkAvailable = { group ->
            group.getNoBotsMembers()
                .map { it.getSQL() }
                .fold(true) { acc, sql ->
                    acc && sql?.accountType == AccountType.ADMIN
                }
        },
    )

    val entries = hashMapOf<String, FeatureType<out FeatureData>>(
        CHAT_SYNC.serializedName to CHAT_SYNC,
        CONSOLE.serializedName to CONSOLE,
    )
    fun registerType(type: FeatureType<*>) {
        entries[type.serializedName] = type
    }
}
