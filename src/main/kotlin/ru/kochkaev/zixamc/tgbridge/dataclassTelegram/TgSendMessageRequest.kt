package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
//data class TgSendMessageRequest(
//    @SerializedName("chat_id")
//    val chatId: Long,
//    @SerializedName("text")
//    val text: String,
//    @SerializedName("reply_to_message_id")
//    val replyToMessageId: Int? = null,
//    @SerializedName("parse_mode")
//    val parseMode: String = "HTML",
//    @SerializedName("disable_web_page_preview")
//    val disableWebPagePreview: Boolean = true,
//    val entities: List<TgEntity>? = null,
//)
data class TgSendMessageRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("message_thread_id")
    val messageThreadId: Int? = null,
    @SerializedName("text")
    val text: String,
    @SerializedName("parse_mode")
    val parseMode: String = "HTML",
    val entities: List<TgEntity>? = null,
    @SerializedName("message_effect_id")
    val messageEffectId: String? = null,
    @SerializedName("reply_parameters")
    val replyParameters: TgReplyParameters? = null,
    @SerializedName("reply_markup")
    val replyMarkup: TgReplyMarkup? = null,
)
