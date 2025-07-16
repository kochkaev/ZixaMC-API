package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents reaction changes on a message with anonymous reactions. */
data class TgMessageReactionCountUpdated(
    /** The chat containing the message */
    val chat: TgChat,
    /** Unique message identifier inside the chat */
    @SerializedName("message_id")
    val messageId: Int,
    /** Date of the change in Unix time */
    val date: Int,
    /** List of reactions that are present on the message */
    val reactions: List<TgReactionCount>,
)
