package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a general file (as opposed to photos, voice messages and audio files). */
open class TgDocument(
    /** Identifier for this file, which can be used to download or reuse the file */
    @SerializedName("file_id")
    val fileId: String,
    /** Unique identifier for this file, which is supposed to be the same over time and for different bots. Can't be used to download or reuse the file. */
    @SerializedName("file_unique_id")
    val fileUniqueId: String,
    /** Original filename as defined by the sender */
    @SerializedName("file_name")
    /** Document thumbnail as defined by the sender */
    val thumbnail: TgPhotoSize?,
    val fileName: String?,
    /** MIME type of the file as defined by the sender */
    @SerializedName("mime_type")
    val mimeType: String?,
    /** File size in bytes. It can be bigger than 2^31 and some programming languages may have difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so a signed 64-bit integer or double-precision float type are safe for storing this value. */
    @SerializedName("file_size")
    val fileSize: Long?,
)
