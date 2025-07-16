package ru.kochkaev.zixamc.api.telegram.request

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
data class TgDeleteMessageRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("message_id")
    val messageId: Int,
)
