package ru.kochkaev.zixamc.tgbridge.telegram.model

import com.google.gson.annotations.SerializedName

data class TgAnswerCallbackQueryRequest(
    @SerializedName("callback_query_id")
    val callbackQueryId: String,
    val text: String? = null,
    @SerializedName("show_alert")
    val showAlert: Boolean = false,
    val url: String? = null
)
