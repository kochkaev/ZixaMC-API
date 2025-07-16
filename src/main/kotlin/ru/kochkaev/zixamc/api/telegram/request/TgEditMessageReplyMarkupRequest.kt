package ru.kochkaev.zixamc.api.telegram.request

import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup

data class TgEditMessageReplyMarkupRequest(
    val chat_id: Long? = null,
    val message_id: Int? = null,
    val inline_message_id: String? = null,
    val reply_markup: TgReplyMarkup
)
