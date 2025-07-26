package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** Represents a reaction added to a message along with the number of times it was added. */
data class TgReactionCount(
    /** Type of the reaction */
    val type: TgReactionType,
    /** Number of times the reaction was added */
    @SerializedName("total_count")
    val totalCount: Int,
)
