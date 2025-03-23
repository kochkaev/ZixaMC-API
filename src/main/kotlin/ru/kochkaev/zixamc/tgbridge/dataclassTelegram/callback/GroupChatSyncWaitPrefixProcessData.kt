package ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback

data class GroupChatSyncWaitPrefixProcessData(
    val topicId: Int?,
    val messageId: Int,
    val type: PrefixTypes = PrefixTypes.DEFAULT,
    val callbacksToDelete: List<Long> = listOf(),
): ProcessData {
    enum class PrefixTypes {
        DEFAULT,
        FROM_MINECRAFT,
    }
}