package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object contains information about a chat that was shared with the bot using a KeyboardButtonRequestChat button. */
data class TgChatShared(
    /** Identifier of the request */
    @SerializedName("request_id")
    val requestId: String,
    /** Identifier of the shared chat. This number may have more than 32 significant bits and some programming languages may have difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so a 64-bit integer or double-precision float type are safe for storing this identifier. The bot may not have access to the chat and could be unable to use this identifier, unless the chat is already known to the bot by some other means. */
    @SerializedName("chat_id")
    val chatId: Long,
    /** Title of the chat, if the title was requested by the bot. */
    val title: String?,
    /** Username of the chat, if the username was requested by the bot and available. */
    val username: String?,
    /** Available sizes of the chat photo, if the photo was requested by the bot */
    val photo: List<TgPhotoSize>?,
)
