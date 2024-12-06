package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

import com.google.gson.annotations.SerializedName

/**
 * @author kochkaev
 */
data class TgForwardMessageRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("message_thread_id")
    val messageThreadId: Int? = null,
    @SerializedName("from_chat_id")
    val fromChatId: Long,
    @SerializedName("disable_notification")
    val disableNotification: Boolean = false,
    @SerializedName("protect_content")
    val protectContent: Boolean = false,
    @SerializedName("message_id")
    val messageId: Int,
)
