package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** Represents a join request sent to a chat. */
data class TgChatJoinRequest(
    /** Chat to which the request was sent */
    val chat: TgChat,
    /** User that sent the join request */
    val from: TgUser,
    /** Identifier of a private chat with the user who sent the join request. This number may have more than 32 significant bits and some programming languages may have difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so a 64-bit integer or double-precision float type are safe for storing this identifier. The bot can use this identifier for 5 minutes to send messages until the join request is processed, assuming no other administrator contacted the user. */
    @SerializedName("user_chat_id")
    val userChatId: Long,
    /** Date the request was sent in Unix time */
    val date: Int,
    /** Bio of the user. */
    val bio: String? = null,
    /** Chat invite link that was used by the user to send the join request */
    @SerializedName("invite_link")
    val inviteLink: TgChatInviteLink? = null,
)

/** Represents an invite link for a chat. */
data class TgChatInviteLink(
    /** The invite link. If the link was created by another chat administrator, then the second part of the link will be replaced with “…”. */
    @SerializedName("invite_link")
    val inviteLink: String,
    /** Creator of the link */
    val creator: TgUser,
    /** True, if users joining the chat via the link need to be approved by chat administrators */
    @SerializedName("creates_join_request")
    val createsJoinRequest: Boolean,
    /** True, if the link is primary */
    @SerializedName("is_primary")
    val isPrimary: Boolean,
    /** True, if the link is revoked */
    @SerializedName("is_revoked")
    val isRevoked: Boolean,
    /** Optional. Invite link name */
    val name: String? = null,
    /** Point in time (Unix timestamp) when the link will expire or has been expired */
    @SerializedName("expire_date")
    val expireDate: Int? = null,
    /** The maximum number of users that can be members of the chat simultaneously after joining the chat via this invite link; 1-99999 */
    @SerializedName("member_limit")
    val memberLimit: Int? = null,
    /** Number of pending join requests created using this link */
    @SerializedName("pending_join_request_count")
    val pendingJoinRequestCount: Int? = null,
    /** The number of seconds the subscription will be active for before the next payment */
    @SerializedName("subscription_period")
    val subscriptionPeriod: Int? = null,
    /** The amount of Telegram Stars a user must pay initially and after each subsequent subscription period to be a member of the chat using the link */
    @SerializedName("subscription_price")
    val subscriptionPrice: Int? = null,
)
