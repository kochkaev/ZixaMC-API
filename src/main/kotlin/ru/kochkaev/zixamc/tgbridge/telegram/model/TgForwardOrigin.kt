package ru.kochkaev.zixamc.tgbridge.telegram.model

data class TgForwardOrigin(
    val type: String,
    val sender_user: TgUser? = null,
    val sender_user_name: String? = null,
    val sender_chat: TgChat? = null,
)
