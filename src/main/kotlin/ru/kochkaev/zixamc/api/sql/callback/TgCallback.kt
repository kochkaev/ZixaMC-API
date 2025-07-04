package ru.kochkaev.zixamc.api.sql.callback

data class TgCallback<T: CallbackData> (
    val type: String,
    val data: T? = null,
)