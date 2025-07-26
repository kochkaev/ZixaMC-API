package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object contains information about the quoted part of a message that is replied to by the given message. */
data class TgTextQuote(
    /** Text of the quoted part of a message that is replied to by the given message */
    val text: String,
    /** Special entities that appear in the quote. Currently, only bold, italic, underline, strikethrough, spoiler, and custom_emoji entities are kept in quotes. */
    val entities: List<TgEntity>? = null,
    /** Approximate quote position in the original message in UTF-16 code units as specified by the sender */
    val position: Int? = null,
    /** True, if the quote was chosen manually by the message sender. Otherwise, the quote was added automatically by the server. */
    @SerializedName("is_manual")
    val isManual: Boolean? = null,
)
