package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/**
 * @author kochkaev
 */
data class TgStopPollRequest(
    @SerializedName("business_connection_id")
    val businessConnectionId: String? = null,
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("message_id")
    val messageId: Int,
    @SerializedName("reply_markup")
    val replyMarkup: TgInlineKeyboardMarkup? = null,
)
