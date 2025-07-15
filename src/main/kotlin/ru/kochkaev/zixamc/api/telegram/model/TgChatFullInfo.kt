package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object contains full information about a chat. */
class TgChatFullInfo(
    id: Long,
    type: TgChatType,
    title: String? = "",
    username: String? = null,
    firstName: String? = null,
    lastName: String? = null,
    isForum: Boolean = false,
    /** Identifier of the accent color for the chat name and backgrounds of the chat photo, reply header, and link preview. See accent colors for more details. */
    @SerializedName("accent_color_id")
    val accentColorId: Int,
    /** The maximum number of reactions that can be set on a message in the chat */
    @SerializedName("max_reaction_count")
    val maxReactionCount: Int,
    /** Chat photo */
    val photo: TgChatPhoto?,
    /** If non-empty, the list of all active chat usernames; for private chats, supergroups and channels */
    @SerializedName("active_usernames")
    val activeUsernames: List<String>?,
    /** For private chats, the date of birth of the user */
    val birthdate: TgBirthdate?,
    /** For private chats with business accounts, the intro of the business */
    @SerializedName("business_intro")
    val businessIntro: TgBusinessIntro?,
    /** For private chats with business accounts, the location of the business */
    @SerializedName("business_location")
    val businessLocation: TgBusinessLocation?,
    /** For private chats with business accounts, the opening hours of the business */
    @SerializedName("business_opening_hours")
    val businessOpeningHours: TgBusinessOpeningHours?,
    /** For private chats, the personal channel of the user */
    @SerializedName("personal_chat")
    val personalChat: TgChat?,
    /** List of available reactions allowed in the chat. If omitted, then all emoji reactions are allowed. */
    @SerializedName("available_reactions")
    val availableReactions: List<TgReactionType>?,
    /** Custom emoji identifier of the emoji chosen by the chat for the reply header and link preview background */
    @SerializedName("background_custom_emoji_id")
    val backgroundCustomEmojiId: Int?,
    /** Identifier of the accent color for the chat's profile background. See profile accent colors for more details. */
    @SerializedName("profile_accent_color_id")
    val profileAccentColorId: Int?,
    /** Custom emoji identifier of the emoji chosen by the chat for its profile background */
    @SerializedName("profile_accent_color_id")
    val profileBackgroundCustomEmojiId: String?,
    /** Custom emoji identifier of the emoji status of the chat or the other party in a private chat */
    @SerializedName("emoji_status_custom_emoji_id")
    val emojiStatusCustomEmojiId: String?,
    /** Expiration date of the emoji status of the chat or the other party in a private chat, in Unix time, if any */
    @SerializedName("emoji_status_expiration_date")
    val emojiStatusExpirationDate: Int?,
    /** Bio of the other party in a private chat */
    val bio: String?,
    /** True, if privacy settings of the other party in the private chat allows to use tg://user?id=&ltuser_id&gt links only in chats with the user */
    @SerializedName("has_private_forwards")
    val hasPrivateForwards: Boolean?,
    /** True, if the privacy settings of the other party restrict sending voice and video note messages in the private chat */
    @SerializedName("has_restricted_voice_and_video_messages")
    val hasRestrictedVoiceAndVideoMessages: Boolean?,
    /** True, if users need to join the supergroup before they can send messages */
    @SerializedName("join_to_send_messages")
    val joinToSendMessages: Boolean?,
    /** True, if all users directly joining the supergroup without using an invite link need to be approved by supergroup administrators */
    @SerializedName("join_by_request")
    val joinByRequest: Boolean?,
    /** Description, for groups, supergroups and channel chats */
    val description: String?,
    /** Primary invite link, for groups, supergroups and channel chats */
    @SerializedName("invite_link")
    val inviteLink: String?,
    /** The most recent pinned message (by sending date) */
    @SerializedName("pinned_message")
    val pinnedMessage: TgMessage?,
    /** Default chat member permissions, for groups and supergroups */
    @SerializedName("pinned_message")
    val permissions: TgChatPermissions?,
    /** Information about types of gifts that are accepted by the chat or by the corresponding user for private chats */
    @SerializedName("accepted_gift_types")
    val acceptedGiftTypes: TgAcceptedGiftTypes,
    /** True, if paid media messages can be sent or forwarded to the channel chat. The field is available only for channel chats. */
    @SerializedName("can_send_paid_media")
    val canSendPaidMedia: Boolean?,
    /** For supergroups, the minimum allowed delay between consecutive messages sent by each unprivileged user; in seconds */
    @SerializedName("slow_mode_delay")
    val slowModeDelay: Int?,
    /** For supergroups, the minimum number of boosts that a non-administrator user needs to add in order to ignore slow mode and chat permissions */
    @SerializedName("unrestrict_boost_count")
    val unrestrictBoostCount: Int?,
    /** The time after which all messages sent to the chat will be automatically deleted; in seconds */
    @SerializedName("message_auto_delete_time")
    val messageAutoDeleteTime: Int?,
    /** True, if aggressive anti-spam checks are enabled in the supergroup. The field is only available to chat administrators.*/
    @SerializedName("has_aggressive_anti_spam_enabled")
    val hasAggressiveAntiSpamEnabled: Boolean?,
    /** True, if non-administrators can only get the list of bots and administrators in the chat */
    @SerializedName("has_hidden_members")
    val hasHiddenMembers: Boolean?,
    /** True, if messages from the chat can't be forwarded to other chats */
    @SerializedName("has_protected_content")
    val hasProtectedContent: Boolean?,
    /** True, if new chat members will have access to old messages; available only to chat administrators */
    @SerializedName("has_visible_history")
    val hasVisibleHistory: Boolean?,
    /** For supergroups, name of the group sticker set */
    @SerializedName("sticker_set_name")
    val stickerSetName: String?,
    /** True, if the bot can change the group sticker set */
    @SerializedName("can_set_sticker_set")
    val canSetStickerSet: Boolean?,
    /** For supergroups, the name of the group's custom emoji sticker set. Custom emoji from this set can be used by all users and bots in the group. */
    @SerializedName("custom_emoji_sticker_set_name")
    val customEmojiStickerSetName: String?,
    /** Unique identifier for the linked chat, i.e. the discussion group identifier for a channel and vice versa; for supergroups and channel chats. This identifier may be greater than 32 bits and some programming languages may have difficulty/silent defects in interpreting it. But it is smaller than 52 bits, so a signed 64 bit integer or double-precision float type are safe for storing this identifier. */
    @SerializedName("linked_chat_id")
    val linkedChatId: Int?,
    /** For supergroups, the location to which the supergroup is connected */
    val location: TgChatLocation?,
): TgChat(id, type, title, username, firstName, lastName, isForum)
