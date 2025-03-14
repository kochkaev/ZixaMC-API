package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

data class TgAudio(
    val file_id: String,
    val file_unique_id: String,
    val duration: Int?,
    val performer: String?,
    val title: String?,
    val file_name: String?,
    val mime_type: String?,
    val file_size: Long?,
)
