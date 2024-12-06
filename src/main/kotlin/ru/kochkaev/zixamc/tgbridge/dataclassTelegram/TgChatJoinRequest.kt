package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

data class TgChatJoinRequest(
    val chat: TgChat,
    val from: TgUser,
    val user_chat_id: Long,
    val date: Int,
    val bio: String? = null,
    val invite_link: TgChatInviteLink? = null,
) {
    data class TgChatInviteLink(
        val invite_link: String,
        val creator: TgUser,
        val creates_join_request: Boolean,
        val is_primary: Boolean,
        val is_revoked: Boolean,
        val name: String? = null,
        val expire_date: Int? = null,
        val member_limit: Int? = null,
        val pending_join_request_count: Int? = null,
        val subscription_period: Int? = null,
        val subscription_price: Int? = null,
    )
}
