package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

data class TgUnbanChatMemberRequest(
    val chat_id: Long,
    val user_id: Long,
    val only_if_banned: Boolean,
)
