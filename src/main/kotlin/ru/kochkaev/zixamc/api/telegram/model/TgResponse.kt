package ru.kochkaev.zixamc.api.telegram.model

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
    val parameters: TgResponseParameters?
)
/** Describes why a request was unsuccessful. */
data class TgResponseParameters(
    /** The group has been migrated to a supergroup with the specified identifier. This number may have more than 32 significant bits and some programming languages may have difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so a signed 64-bit integer or double-precision float type are safe for storing this identifier. */
    @SerializedName("migrate_to_chat_id")
    val migrateToChatId: Int? = null,
    /** In case of exceeding flood control, the number of seconds left to wait before the request can be repeated */
    @SerializedName("retry_after")
    val retryAfter: Long?
)