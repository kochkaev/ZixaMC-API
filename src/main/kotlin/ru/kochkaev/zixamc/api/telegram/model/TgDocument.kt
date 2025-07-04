package ru.kochkaev.zixamc.api.telegram.model

data class TgDocument(
    val file_id: String,
    val file_unique_id: String,
    val file_name: String?,
    val mime_type: String?,
    val file_size: Long?,
)
