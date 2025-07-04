package ru.kochkaev.zixamc.api.sql.process

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