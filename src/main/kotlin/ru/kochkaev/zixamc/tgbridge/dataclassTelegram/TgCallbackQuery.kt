package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

data class TgCallbackQuery(
    val id: String,
    val from: TgUser,
    val message: TgMessage,
    val inline_message_id: String? = null,
    val chat_instance: String? = null,
    val data: String? = null,
)
