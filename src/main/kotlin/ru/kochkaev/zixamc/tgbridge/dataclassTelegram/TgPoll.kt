package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

/**
 * @author vanutp
 */
data class TgPoll(
    val question: String,
    val options: List<PollOption>? = null,
) {
    data class PollOption (
        val text: String,
        val text_entities: List<TgEntity>?,
        val voter_count: Int,
    )
}
