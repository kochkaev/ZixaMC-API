package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a change of a reaction on a message performed by a user. */
data class TgMessageReactionUpdated(
    /** The chat containing the message the user reacted to */
    val chat: TgChat,
    /** Unique identifier of the message inside the chat */
    @SerializedName("message_id")
    val messageId: Int,
    /** The user that changed the reaction, if the user isn't anonymous */
    val user: TgUser?,
    /** The chat on behalf of which the reaction was changed, if the user is anonymous */
    @SerializedName("actor_chat")
    val actorChat: TgChat?,
    /** Date of the change in Unix time */
    val date: Int,
    /** Previous list of reaction types that were set by the user */
    @SerializedName("old_reaction")
    val oldReaction: List<TgReactionType>,
    /** New list of reaction types that have been set by the user */
    @SerializedName("new_reaction")
    val newReaction: List<TgReactionType>,
)
