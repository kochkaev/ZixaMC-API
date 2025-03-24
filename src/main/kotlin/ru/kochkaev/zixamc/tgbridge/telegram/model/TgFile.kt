package ru.kochkaev.zixamc.tgbridge.telegram.model

data class TgFile(
    val file_id: String,
    val file_unique_id: String,
    val file_size: Long?,
    val file_path: String?,
)
