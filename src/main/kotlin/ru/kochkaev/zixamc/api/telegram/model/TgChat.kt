package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a chat. */
open class TgChat(
    /** Unique identifier for this chat. This number may have more than 32 significant bits and some programming languages may have difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so a signed 64-bit integer or double-precision float type are safe for storing this identifier. */
    open val id: Long,
    /** Type of the chat, can be either “private”, “group”, “supergroup” or “channel” */
    open val type: TgChatType,
    /** Title, for supergroups, channels and group chats */
    open val title: String? = null,
    /** Username, for private chats, supergroups and channels if available */
    open val username: String? = null,
    /** First name of the other party in a private chat */
    @SerializedName("first_name")
    open val firstName: String? = null,
    /** Last name of the other party in a private chat */
    @SerializedName("last_name")
    open val lastName: String? = null,
    /** True, if the supergroup chat is a forum (has topics enabled) */
    @SerializedName("is_forum")
    open val isForum: Boolean? = false,
)
