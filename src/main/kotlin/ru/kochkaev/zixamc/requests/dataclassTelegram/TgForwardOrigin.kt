package ru.kochkaev.zixamc.requests.dataclassTelegram

data class TgForwardOrigin(
    val type: String,
    val sender_user: TgUser? = null,
    val sender_user_name: String? = null,
    val sender_chat: TgChat? = null,
)
