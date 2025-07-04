package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/**
 * @author kochkaev
 */
data class TgInputPollOption(
    val text: String,
    @SerializedName("text_parse_mode")
    val textParseMode: String = "HTML",
    @SerializedName("text_entities")
    val textEntities: List<TgEntity>? = null,
)
