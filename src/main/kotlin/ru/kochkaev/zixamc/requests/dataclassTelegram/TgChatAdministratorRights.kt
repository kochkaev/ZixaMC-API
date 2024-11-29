package ru.kochkaev.zixamc.requests.dataclassTelegram

data class TgChatAdministratorRights(
    val is_anonymous: Boolean,
    val can_manage_chat: Boolean,
    val can_delete_messages: Boolean,
    val can_manage_video_chats: Boolean,
    val can_restrict_members: Boolean,
    val can_promote_members: Boolean,
    val can_change_info: Boolean,
    val can_invite_users: Boolean,
    val can_post_stories: Boolean,
    val can_edit_stories: Boolean,
    val can_delete_stories: Boolean,
    val can_post_messages: Boolean? = null,
    val can_edit_messages: Boolean? = null,
    val can_pin_messages: Boolean? = null,
    val can_manage_topics: Boolean? = null,
)
