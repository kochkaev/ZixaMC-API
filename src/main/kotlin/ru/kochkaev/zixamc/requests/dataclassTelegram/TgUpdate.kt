package ru.kochkaev.zixamc.requests.dataclassTelegram

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
data class TgUpdate(
    @SerializedName("update_id")
    val updateId: Int,
    val message: TgMessage? = null,
    @SerializedName("callback_query")
    val callbackQuery: TgCallbackQuery? = null
)
