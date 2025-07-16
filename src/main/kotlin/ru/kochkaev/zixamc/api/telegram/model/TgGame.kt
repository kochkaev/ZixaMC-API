package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a game. Use BotFather to create and edit games, their short names will act as unique identifiers. */
data class TgGame(
    /** Title of the game */
    val title: String,
    /** Description of the game */
    val description: String,
    /** Photo that will be displayed in the game message in chats. */
    val photo: TgPhotoSize,
    /** Brief description of the game or high scores included in the game message. Can be automatically edited to include current high scores for the game when the bot calls setGameScore, or manually edited using editMessageText. 0-4096 characters. */
    val text: String? = null,
    /** Special entities that appear in text, such as usernames, URLs, bot commands, etc. */
    @SerializedName("text_entities")
    val textEntities: List<TgEntity>? = null,
    /** Animation that will be displayed in the game message in chats. Upload via BotFather */
    val animation: TgAnimation? = null,
)
/** A placeholder, currently holds no information. Use BotFather to set up your game. */
class TgCallbackGame
/** This object represents one row of the high scores table for a game. */
data class TgGameHighScore(
    /** Position in high score table for the game */
    val position: Int,
    /** User */
    val user: TgUser,
    /** Score */
    val score: Int,
)
