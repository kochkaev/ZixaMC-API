package ru.kochkaev.zixamc.api.telegram.model

/**
 * @author vanutp
 */
data class TgTextQuote(
    val text: String,
    val entities: List<TgEntity>?
)
