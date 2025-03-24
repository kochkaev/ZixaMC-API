package ru.kochkaev.zixamc.tgbridge.telegram.model

data class TgUnbanChatMemberRequest(
    val chat_id: Long,
    val user_id: Long,
    val only_if_banned: Boolean,
)
