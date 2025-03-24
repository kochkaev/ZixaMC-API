package ru.kochkaev.zixamc.tgbridge.telegram.model

/**
 * @author vanutp
 */
data class TgResponse<T>(
    val ok: Boolean,
    val result: T?,
    val description: String?,
)
