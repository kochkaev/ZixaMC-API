package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

import com.google.gson.annotations.SerializedName

/**
 * @author kochkaev
 */
data class TgSendPollRequest(
    @SerializedName("business_connection_id")
    val businessConnectionId: String? = null,
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("message_thread_id")
    val messageThreadId: Int? = null,
    val question: String,
    @SerializedName("question_parse_mode")
    val questionParseMode: String = "HTML",
    @SerializedName("question_entities")
    val questionEntities: List<TgEntity>? = null,
    val options: List<TgInputPollOption>,
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean = false,
    val type: String = "regular",
    @SerializedName("allows_multiple_answers")
    val allowsMultipleAnswers: Boolean = false,
    @SerializedName("correct_option_id")
    val correctOptionId: Int? = null,
    val explanation: String? = null,
    @SerializedName("explanation_parse_mode")
    val explanationParseMode: String = "HTML",
    @SerializedName("explanation_entities")
    val explanationEntities: List<TgEntity>? = null,
    @SerializedName("open_period")
    val openPeriod: Int? = null,
    @SerializedName("close_date")
    val closeDate: Int? = null,
    @SerializedName("is_closed")
    val isClosed: Boolean = false,
    @SerializedName("disable_notification")
    val disableNotification: Boolean = false,
    @SerializedName("protect_content")
    val protectContent: Boolean = false,
    @SerializedName("allow_paid_broadcast")
    val allowPaidBroadcast: Boolean = false,
    @SerializedName("message_effect_id")
    val messageEffectId: String? = null,
    @SerializedName("reply_parameters")
    val replyParameters: TgReplyParameters? = null,
)
