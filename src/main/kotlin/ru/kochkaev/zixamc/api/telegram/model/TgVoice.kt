package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a video message (available in Telegram apps as of v.4.0). */
data class TgVoice(
    /** Identifier for this file, which can be used to download or reuse the file */
    val fileId: String,
    /** Unique identifier for this file, which is supposed to be the same over time and for different bots. Can't be used to download or reuse the file. */
    val fileUniqueId: String,
    /** Duration of the audio in seconds as defined by the sender */
    val duration: Int,
    /** MIME type of the file as defined by the sender */
    @SerializedName("mime_type")
    val mimeType: String?,
    /** File size in bytes */
    val fileSize: Int?,
)
