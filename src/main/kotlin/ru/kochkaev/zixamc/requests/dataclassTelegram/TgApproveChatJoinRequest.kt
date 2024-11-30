package ru.kochkaev.zixamc.requests.dataclassTelegram

data class TgApproveChatJoinRequest (
    val chat_id: Long,
    val user_id: Long,
)