package ru.kochkaev.zixamc.tgbridge.telegram.model

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
data class TgResponse<T>(
    val ok: Boolean,
    val result: T?,
    @SerializedName("error_code")
    val errorCode: Int?,
    val description: String?,
    val parameters: Parameters?
) {
    data class Parameters(
        @SerializedName("retry_after")
        val retryAfter: Long?
    )
}
