package ru.kochkaev.zixamc.tgbridge.sql.callback

data class TgCallback<T: CallbackData> (
    val type: String,
    val data: T? = null,
)