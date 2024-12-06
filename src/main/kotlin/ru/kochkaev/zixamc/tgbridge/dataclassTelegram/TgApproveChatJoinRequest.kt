package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

data class TgApproveChatJoinRequest (
    val chat_id: Long,
    val user_id: Long,
)