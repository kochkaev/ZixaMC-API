package ru.kochkaev.zixamc.tgbridge.telegram.model

/**
 * @author kochkaev
 */
data class TgEntity(
    val offset: Int?,
    var length: Int?,
    val type: TgEntityType?,
    var url: String? = null,
)
