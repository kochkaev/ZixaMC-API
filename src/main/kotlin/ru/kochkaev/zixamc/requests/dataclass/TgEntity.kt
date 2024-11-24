package ru.kochkaev.zixamc.requests.dataclass

/**
 * @author kochkaev
 */
data class TgEntity(
    val offset: Int?,
    var length: Int?,
    val type: String?,
    var url: String? = null,
)
