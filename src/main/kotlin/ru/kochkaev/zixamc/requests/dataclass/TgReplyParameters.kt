package ru.kochkaev.zixamc.requests.dataclass

import org.spongepowered.include.com.google.gson.annotations.SerializedName

/**
 * @author kochkaev
 */
data class TgReplyParameters(
    @SerializedName("message_id")
    val messageId: Int,
    @SerializedName("chat_id")
    val chatId: Long? = null,
    @SerializedName("allow_sending_without_reply")
    val allowSendingWithoutReply: Boolean? = null,
    val quote: String? = null,
    @SerializedName("quote_parse_mode")
    val quoteParseMode: String? = null,
    @SerializedName("quote_entities")
    val quoteEntities: List<TgEntity>? = null,
    @SerializedName("quote_position")
    val quotePosition: Int? = null,
)
