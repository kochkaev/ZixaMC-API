package ru.kochkaev.zixamc.api.telegram.request

import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.telegram.model.TgEntity
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters

/**
 * @author vanutp
 */
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
    @SerializedName("protect_content")
    val protectContent: Boolean = false,
    @SerializedName("message_effect_id")
    val messageEffectId: String? = null,
    @SerializedName("reply_parameters")
    val replyParameters: TgReplyParameters? = null,
    @SerializedName("reply_markup")
    val replyMarkup: TgReplyMarkup? = null,
)
