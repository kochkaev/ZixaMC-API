package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

/**
 * @author vanutp
 */
data class TgChat(
    val id: Long,
    val title: String = "",
    val username: String? = null,
)
