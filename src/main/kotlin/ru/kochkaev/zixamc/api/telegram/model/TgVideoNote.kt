package ru.kochkaev.zixamc.api.telegram.model

/** This object represents a video message (available in Telegram apps as of v.4.0). */
data class TgVideoNote(
    /** Identifier for this file, which can be used to download or reuse the file */
    val fileId: String,
    /** Unique identifier for this file, which is supposed to be the same over time and for different bots. Can't be used to download or reuse the file. */
    val fileUniqueId: String,
    /** Video width and height (diameter of the video message) as defined by the sender */
    val length: Int,
    /** Duration of the video in seconds as defined by the sender */
    val duration: Int,
    /** Video thumbnail */
    val thumbnail: TgPhotoSize?,
    /** File size in bytes */
    val fileSize: Int?,
)
