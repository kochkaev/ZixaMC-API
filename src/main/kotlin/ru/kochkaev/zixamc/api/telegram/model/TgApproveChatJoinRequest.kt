package ru.kochkaev.zixamc.api.telegram.model

data class TgApproveChatJoinRequest (
    val chat_id: Long,
    val user_id: Long,
)