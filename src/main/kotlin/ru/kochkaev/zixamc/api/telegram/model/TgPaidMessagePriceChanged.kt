package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** Describes a service message about a change in the price of paid messages within a chat. */
data class TgPaidMessagePriceChanged(
    /** The new number of Telegram Stars that must be paid by non-administrator users of the supergroup chat for each sent message */
    @SerializedName("paid_message_star_count")
    val paidMessageStarCount: Int,
)
