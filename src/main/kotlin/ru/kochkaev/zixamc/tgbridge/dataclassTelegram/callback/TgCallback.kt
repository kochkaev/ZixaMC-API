package ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback

data class TgCallback<T: CallbackData> (
    val type: String,
    val data: T? = null,
)