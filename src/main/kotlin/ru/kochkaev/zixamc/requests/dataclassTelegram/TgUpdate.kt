package ru.kochkaev.zixamc.requests.dataclassTelegram

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
data class TgUpdate(
    @SerializedName("update_id")
    val updateId: Int,
    val message: TgMessage? = null,
)
