package ru.kochkaev.zixamc.api.telegram.model
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTypeEnum

/** This object represents an incoming inline query. When the user sends an empty query, your bot could return some default or trending results. */
data class TgInlineQuery (
    /** Unique identifier for this query */
    val id: String,
    /** Sender */
    val from: TgUser,
    /** Text of the query (up to 256 characters) */
    val query: String,
    /** Offset of the results to be returned, can be controlled by the bot */
    val offset: String,
    /** Type of the chat from which the inline query was sent. Can be either “sender” for a private chat with the inline query sender, “private”, “group”, “supergroup”, or “channel”. The chat type should be always known for requests sent from official clients and most third-party clients, unless the request was sent from a secret chat */
    @SerializedName("chat_type")
    val chatType: TgChatTypeInline? = null,
    /** Sender location, only for bots that request user location */
    val location: TgLocation? = null,
)
enum class TgChatTypeInline {
    @SerializedName("sender")
    SENDER,
    @SerializedName("private")
    PRIVATE,
    @SerializedName("group")
    GROUP,
    @SerializedName("supergroup")
    SUPERGROUP,
    @SerializedName("channel")
    CHANNEL;

    val type: TgChatType
        get() = TgChatType.entries.firstOrNull { it.name == name } ?: TgChatType.PRIVATE
}
/** This object represents a button to be shown above inline query results. You must use exactly one of the optional fields. */
data class TgInlineQueryResultsButton(
    /** Label text on the button */
    val text: String,
    /** Description of the Web App that will be launched when the user presses the button. The Web App will be able to switch back to the inline mode using the method switchInlineQuery inside the Web App. */
    @SerializedName("web_app")
    val webApp: TgWebAppInfo? = null,
    /**
     * Deep-linking parameter for the /start message sent to the bot when a user presses the button. 1-64 characters, only A-Z, a-z, 0-9, _ and - are allowed.
     *
     * Example: An inline bot that sends YouTube videos can ask the user to connect the bot to their YouTube account to adapt search results accordingly. To do this, it displays a 'Connect your YouTube account' button above the results, or even before showing any. The user presses the button, switches to a private chat with the bot and, in doing so, passes a start parameter that instructs the bot to return an OAuth link. Once done, the bot can offer a switch_inline button so that the user can easily return to the chat where they wanted to use the bot's inline capabilities.
     */
    @SerializedName("start_parameter")
    val startParameter: String? = null,
)
/**
 * This object represents one result of an inline query. Telegram clients currently support results of the following 20 types: InlineQueryResultCachedAudio, InlineQueryResultCachedDocument, InlineQueryResultCachedGif, InlineQueryResultCachedMpeg4Gif, InlineQueryResultCachedPhoto, InlineQueryResultCachedSticker, InlineQueryResultCachedVideo, InlineQueryResultCachedVoice, InlineQueryResultArticle, InlineQueryResultAudio, InlineQueryResultContact, InlineQueryResultGame, InlineQueryResultDocument, InlineQueryResultGif, InlineQueryResultLocation, InlineQueryResultMpeg4Gif, InlineQueryResultPhoto, InlineQueryResultVenue, InlineQueryResultVideo or InlineQueryResultVoice
 *
 * Note: All URLs passed in inline query results will be available to end users and therefore must be assumed to be public.
 */
open class TgInlineQueryResult(
    /** Type of the result */
    val type: TgInlineQueryResultType,
    /** Unique identifier for this result, 1-64 Bytes */
    val id: String,
)
enum class TgInlineQueryResultType: TgTypeEnum {
    @SerializedName("article")
    ARTICLE {
        override val model = TgInlineQueryResultArticle::class.java
    },
    @SerializedName("photo")
    PHOTO {
        override val model = TgInlineQueryResultPhoto::class.java
    },
    @SerializedName("mpeg4_gif")
    MPEG4_GIF {
        override val model = TgInlineQueryResultMpeg4Gif::class.java
    },
    @SerializedName("video")
    VIDEO {
        override val model = TgInlineQueryResultVideo::class.java
    },
    @SerializedName("audio")
    AUDIO {
        override val model = TgInlineQueryResultAudio::class.java
    },
    @SerializedName("voice")
    VOICE {
        override val model = TgInlineQueryResultVoice::class.java
    },
    @SerializedName("document")
    DOCUMENT {
        override val model = TgInlineQueryResultDocument::class.java
    },
    @SerializedName("location")
    LOCATION {
        override val model = TgInlineQueryResultLocation::class.java
    },
    @SerializedName("venue")
    VENUE {
        override val model = TgInlineQueryResultVenue::class.java
    },
    @SerializedName("contact")
    CONTACT {
        override val model = TgInlineQueryResultContact::class.java
    },
    @SerializedName("game")
    GAME {
        override val model = TgInlineQueryResultGame::class.java
    },
}