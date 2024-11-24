package ru.kochkaev.zixamc.requests.dataclassTelegram

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
data class TgEditMessageRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("message_id")
    val messageId: Int,
    @SerializedName("text")
    val text: String,
    @SerializedName("parse_mode")
    val parseMode: String = "HTML",
    @SerializedName("disable_web_page_preview")
    val disableWebPagePreview: Boolean = true,
    val entities: List<TgEntity>? = null,
)
