package ru.kochkaev.zixamc.tgbridge.telegram.model

/**
 * @author vanutp
 */
data class TgTextQuote(
    val text: String,
    val entities: List<TgEntity>?
)
