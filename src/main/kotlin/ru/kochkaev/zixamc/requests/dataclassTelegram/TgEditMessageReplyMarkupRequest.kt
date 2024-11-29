package ru.kochkaev.zixamc.requests.dataclassTelegram

data class TgEditMessageReplyMarkupRequest(
    val chat_id: Long? = null,
    val message_id: Int? = null,
    val inline_message_id: String? = null,
    val reply_markup: TgReplyMarkup
)
