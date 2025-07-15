package ru.kochkaev.zixamc.api.telegram.model
import com.google.gson.annotations.SerializedName

/** Represents the rights of a business bot. */
data class TgBusinessBotRights(
    /** True, if the bot can send and edit messages in the private chats that had incoming messages in the last 24 hours */
    @SerializedName("can_reply")
    val canReply: Boolean? = null,
    /** True, if the bot can mark incoming private messages as read */
    @SerializedName("can_read_messages")
    val canReadMessages: Boolean? = null,
    /** True, if the bot can delete messages sent by the bot */
    @SerializedName("can_delete_sent_messages")
    val canDeleteSentMessages: Boolean? = null,
    /** True, if the bot can delete all private messages in managed chats */
    @SerializedName("can_delete_all_messages")
    val canDeleteAllMessages: Boolean? = null,
    /** True, if the bot can edit the first and last name of the business account */
    @SerializedName("can_edit_name")
    val canEditName: Boolean? = null,
    /** True, if the bot can edit the bio of the business account */
    @SerializedName("can_edit_bio")
    val canEditBio: Boolean? = null,
    /** True, if the bot can edit the profile photo of the business account */
    @SerializedName("can_edit_profile_photo")
    val canEditProfilePhoto: Boolean? = null,
    /** True, if the bot can edit the username of the business account */
    @SerializedName("can_edit_username")
    val canEditUsername: Boolean? = null,
    /** True, if the bot can change the privacy settings pertaining to gifts for the business account */
    @SerializedName("can_change_gift_settings")
    val canChangeGiftSettings: Boolean? = null,
    /** True, if the bot can view gifts and the amount of Telegram Stars owned by the business account */
    @SerializedName("can_view_gifts_and_stars")
    val canViewGiftsAndStars: Boolean? = null,
    /** True, if the bot can convert regular gifts owned by the business account to Telegram Stars */
    @SerializedName("can_convert_gifts_to_stars")
    val canConvertGiftsToStars: Boolean? = null,
    /** True, if the bot can transfer and upgrade gifts owned by the business account */
    @SerializedName("can_transfer_and_upgrade_gifts")
    val canTransferAndUpgradeGifts: Boolean? = null,
    /** True, if the bot can transfer Telegram Stars received by the business account to its own account, or use them to upgrade and transfer gifts */
    @SerializedName("can_transfer_stars")
    val canTransferStars: Boolean? = null,
    /** True, if the bot can post, edit and delete stories on behalf of the business account */
    @SerializedName("can_manage_stories")
    val canManageStories: Boolean? = null,
)
/** Describes the connection of the bot with a business account. */
data class TgBusinessConnection(
    /** Unique identifier of the business connection */
    val id: String,
    /** Business account user that created the business connection */
    val user: TgUser,
    /** Identifier of a private chat with the user who created the business connection. This number may have more than 32 significant bits and some programming languages may have difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so a 64-bit integer or double-precision float type are safe for storing this identifier. */
    @SerializedName("user_chat_id")
    val userChatId: Long,
    /** Date the connection was established in Unix time */
    val date: Int,
    /** Rights of the business bot */
    @SerializedName("user_chat_id")
    val rights: TgBusinessBotRights? = null,
    /** True, if the connection is active */
    @SerializedName("is_enabled")
    val isEnabled: Boolean,
)
/** This object is received when messages are deleted from a connected business account. */
data class TgBusinessMessagesDeleted(
    /** Unique identifier of the business connection */
    @SerializedName("business_connection_id")
    val businessConnectionId: String,
    /** Information about a chat in the business account. The bot may not have access to the chat or the corresponding user. */
    val chat: TgChat,
    /** The list of identifiers of deleted messages in the chat of the business account */
    @SerializedName("message_ids")
    val messageIds: List<Int>,
)