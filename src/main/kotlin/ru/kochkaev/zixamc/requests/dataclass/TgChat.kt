package ru.kochkaev.zixamc.requests.dataclass

/**
 * @author vanutp
 */
data class TgChat(
    val id: Long,
    val title: String = "",
    val username: String? = null,
)
