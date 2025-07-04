package ru.kochkaev.zixamc.api.sql.data

data class ProtectedMessageData (
    val message_id: Long,
    val chat_id: Long,
    val protected_type: String,
    val protect_level: Int,
    val sender_bot_id: Long,
)