package ru.kochkaev.zixamc.api.telegram.request

data class TgBanChatMemberRequest(
    val chat_id: Long,
    val user_id: Long,
    val until_date: Long? = null,
    val revoke_messages: Boolean? = null
)
