package ru.kochkaev.zixamc.api.telegram.request

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
data class TgDeleteMessagesRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("message_ids")
    val messageIds: List<Int>,
)
