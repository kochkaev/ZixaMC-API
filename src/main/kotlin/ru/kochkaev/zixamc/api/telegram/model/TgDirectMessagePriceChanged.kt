package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** Describes a service message about a change in the price of paid messages within a chat. */
data class TgDirectMessagePriceChanged(
    /** True, if direct messages are enabled for the channel chat; false otherwise */
    @SerializedName("are_direct_messages_enabled")
    val areDirectMessagesEnabled: Boolean,
    /** The new number of Telegram Stars that must be paid by users for each direct message sent to the channel. Does not apply to users who have been exempted by administrators. Defaults to 0. */
    @SerializedName("direct_message_star_count")
    val directMessageStarCount: Int? = 0,
)
