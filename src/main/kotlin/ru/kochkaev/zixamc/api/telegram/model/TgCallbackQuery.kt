package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/**
 * This object represents an incoming callback query from a callback button in an inline keyboard. If the button that originated the query was attached to a message sent by the bot, the field message will be present. If the button was attached to a message sent via the bot (in inline mode), the field inline_message_id will be present. Exactly one of the fields data or game_short_name will be present.
 *
 * NOTE: After the user presses a callback button, Telegram clients will display a progress bar until you call answerCallbackQuery. It is, therefore, necessary to react by calling answerCallbackQuery even if no notification to the user is needed (e.g., without specifying any of the optional parameters).
 */
data class TgCallbackQuery(
    /** Unique identifier for this query */
    val id: String,
    /** Sender */
    val from: TgUser,
    /** Message sent by the bot with the callback button that originated the query */
    val message: TgMaybeInaccessibleMessage?,
    /** Identifier of the message sent via the bot in inline mode, that originated the query. */
    @SerializedName("inline_message_id")
    val inlineMessageId: String? = null,
    /** Global identifier, uniquely corresponding to the chat to which the message with the callback button was sent. Useful for high scores in games. */
    @SerializedName("chat_instance")
    val chatInstance: String,
    /** Data associated with the callback button. Be aware that the message originated the query can contain no callback buttons with this data. */
    val data: String? = null,
    /** Short name of a Game to be returned, serves as the unique identifier for the game */
    @SerializedName("game_short_name")
    val gameShortName: String? = null,
)
