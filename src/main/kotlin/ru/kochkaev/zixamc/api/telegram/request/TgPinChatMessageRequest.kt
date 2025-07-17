package ru.kochkaev.zixamc.api.telegram.request

data class TgPinChatMessageRequest(
    val chat_id: Long,
    val message_id: Long,
    val disable_notification: Boolean = false,
)
