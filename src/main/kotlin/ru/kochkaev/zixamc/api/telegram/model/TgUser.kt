package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a Telegram user or bot. */
data class TgUser(
    /** Unique identifier for this user or bot. This number may have more than 32 significant bits and some programming languages may have difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so a 64-bit integer or double-precision float type are safe for storing this identifier. */
    val id: Long,
    /** True, if this user is a bot */
    @SerializedName("is_bot")
    val isBot: Boolean = false,
    /** User's or bot's first name */
    @SerializedName("first_name")
    val firstName: String,
    /** User's or bot's last name */
    @SerializedName("last_name")
    val lastName: String?,
    /** User's or bot's username */
    val username: String?,
    /** IEFT language tag of the user's language */
    @SerializedName("language_code")
    val languageCode: String?,
    /** True, if this user is a Telegram Premium user */
    @SerializedName("is_premium")
    val isPremium: Boolean?,
    /** True, if this user added the bot to the attachment menu */
    @SerializedName("added_to_attachment_menu")
    val addedToAttachmentMenu: Boolean?,
    /** True, if the bot can be invited to groups. Returned only in getMe */
    @SerializedName("can_join_groups")
    val canJoinGroups: Boolean?,
    /** True, if privacy mode is disabled for the bot. Returned only in getMe */
    @SerializedName("can_read_all_group_messages")
    val canReadAllGroupMessages: Boolean?,
    /** True, if the bot supports inline queries. Returned only in getMe */
    @SerializedName("supports_inline_queries")
    val supportsInlineQueries: Boolean?,
    /** True, if the bot can be connected to a Telegram Business account to receive its messages. Returned only in getMe */
    @SerializedName("can_connect_to_business")
    val canConnectToBusiness: Boolean?,
    /** True, if the bot has a main Web App. Returned only in getMe */
    @SerializedName("has_main_web_app")
    val hasMainWebApp: Boolean?,
)
