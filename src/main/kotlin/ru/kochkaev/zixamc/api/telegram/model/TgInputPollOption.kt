package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object contains information about one answer option in a poll to be sent. */
data class TgInputPollOption(
    /** Option text, 1-100 characters */
    val text: String,
    /** Mode for parsing entities in the text. See formatting options for more details. Currently, only custom emoji entities are allowed */
    @SerializedName("text_parse_mode")
    val textParseMode: String = "HTML",
    /** A JSON-serialized list of special entities that appear in the poll option text. It can be specified instead of text_parse_mode */
    @SerializedName("text_entities")
    val textEntities: List<TgEntity>? = null,
)
