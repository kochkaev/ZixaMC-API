package ru.kochkaev.zixamc.requests.dataclass

/**
 * @author vanutp
 */
data class TgResponse<T>(
    val ok: Boolean,
    val result: T?,
    val description: String?,
)
