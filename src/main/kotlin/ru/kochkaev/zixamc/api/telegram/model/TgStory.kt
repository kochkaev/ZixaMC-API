package ru.kochkaev.zixamc.api.telegram.model

/** This object represents a story. */
data class TgStory(
    /** Chat that posted the story */
    val chat: TgChat,
    /** Unique identifier for the story in the chat */
    val id: Int,
)
