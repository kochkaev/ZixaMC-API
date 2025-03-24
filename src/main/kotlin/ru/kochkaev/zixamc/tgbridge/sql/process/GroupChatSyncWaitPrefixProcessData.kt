package ru.kochkaev.zixamc.tgbridge.sql.process

class GroupChatSyncWaitPrefixProcessData(
    val topicId: Int?,
    messageId: Int,
    val type: PrefixTypes = PrefixTypes.DEFAULT
): ProcessData(messageId) {
    enum class PrefixTypes {
        DEFAULT,
        FROM_MINECRAFT,
    }
}