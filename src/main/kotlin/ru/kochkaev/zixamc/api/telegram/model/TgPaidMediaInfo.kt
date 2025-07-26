package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** Describes the paid media added to a message. */
data class TgPaidMediaInfo(
    /** The number of Telegram Stars that must be paid to buy access to the media */
    @SerializedName("star_count")
    val starCount: Int,
    /** Information about the paid media */
    @SerializedName("paid_media")
    val paidMedia: List<TgPaidMedia>,
)
