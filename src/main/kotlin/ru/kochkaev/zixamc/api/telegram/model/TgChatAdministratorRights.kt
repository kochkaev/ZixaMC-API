package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** Represents the rights of an administrator in a chat. */
data class TgChatAdministratorRights(
    /** True, if the user's presence in the chat is hidden */
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean,
    /** True, if the administrator can access the chat event log, get boost list, see hidden supergroup and channel members, report spam messages, ignore slow mode, and send messages to the chat without paying Telegram Stars. Implied by any other administrator privilege. */
    @SerializedName("can_manage_chat")
    val canManageChat: Boolean,
    /** True, if the administrator can delete messages of other users */
    @SerializedName("can_delete_messages")
    val canDeleteMessages: Boolean,
    /** True, if the administrator can manage video chats */
    @SerializedName("can_manage_video_chats")
    val canManageVideoChats: Boolean,
    /** True, if the administrator can restrict, ban or unban chat members, or access supergroup statistics */
    @SerializedName("can_restrict_members")
    val canRestrictMembers: Boolean,
    /**  	True, if the administrator can add new administrators with a subset of their own privileges or demote administrators that they have promoted, directly or indirectly (promoted by administrators that were appointed by the user) */
    @SerializedName("can_promote_members")
    val canPromoteMembers: Boolean,
    /** True, if the user is allowed to change the chat title, photo and other settings */
    @SerializedName("can_change_info")
    val canChangeInfo: Boolean,
    /** True, if the user is allowed to invite new users to the chat */
    @SerializedName("can_invite_users")
    val canInviteUsers: Boolean,
    /** True, if the administrator can post stories to the chat */
    @SerializedName("can_post_stories")
    val canPostStories: Boolean,
    /** True, if the administrator can edit stories posted by other users, post stories to the chat page, pin chat stories, and access the chat's story archive */
    @SerializedName("can_edit_stories")
    val canEditStories: Boolean,
    /** True, if the administrator can delete stories posted by other users */
    @SerializedName("can_delete_stories")
    val canDeleteStories: Boolean,
    /** True, if the administrator can post messages in the channel, approve suggested posts, or access channel statistics; for channels only */
    @SerializedName("can_post_messages")
    val canPostMessages: Boolean? = null,
    /** True, if the administrator can edit messages of other users and can pin messages; for channels only */
    @SerializedName("can_edit_messages")
    val canEditMessages: Boolean? = null,
    /** True, if the user is allowed to pin messages; for groups and supergroups only */
    @SerializedName("can_pin_messages")
    val canPinMessages: Boolean? = null,
    /** True, if the user is allowed to create, rename, close, and reopen forum topics; for supergroups only */
    @SerializedName("can_manage_topics")
    val canManageTopics: Boolean? = null,
)
