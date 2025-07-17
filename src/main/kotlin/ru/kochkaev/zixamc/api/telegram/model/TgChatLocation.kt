package ru.kochkaev.zixamc.api.telegram.model

/** Represents a location to which a chat is connected. */
data class TgChatLocation(
    /** The location to which the supergroup is connected. Can't be a live location. */
    val location: TgLocation,
    /** Location address; 1-64 characters, as defined by the chat owner */
    val address: String,
)
