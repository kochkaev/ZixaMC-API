package ru.kochkaev.zixamc.tgbridge.telegram.model

data class TgPinChatMessageRequest(
    val chat_id: Long,
    val message_id: Long,
    val disable_notification: Boolean = false,
)
