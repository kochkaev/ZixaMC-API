package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents changes in the status of a chat member. */
data class TgChatMemberUpdated(
    /** Chat the user belongs to */
    val chat: TgChat,
    /** Performer of the action, which resulted in the change */
    val from: TgUser,
    /** Date the change was done in Unix time */
    val date: Int,
    /** Previous information about the chat member */
    @SerializedName("old_chat_member")
    val oldChatMember: TgChatMember,
    /** New information about the chat member */
    @SerializedName("new_chat_member")
    val newChatMember: TgChatMember,
    /** Chat invite link, which was used by the user to join the chat; for joining by invite link events only. */
    @SerializedName("invite_link")
    val inviteLink: TgChatInviteLink?,
    /** True, if the user joined the chat after sending a direct join request without using an invite link and being approved by an administrator */
    @SerializedName("via_join_request")
    val viaJoinRequest: Boolean?,
    /** True, if the user joined the chat via a chat folder invite link */
    @SerializedName("via_chat_folder_invite_link")
    val viaChatFolderInviteLink: Boolean?,
)
