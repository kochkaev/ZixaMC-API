package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents one size of a photo or a file / sticker thumbnail. */
data class TgPhotoSize(
    /** Identifier for this file, which can be used to download or reuse the file */
    @SerializedName("file_id")
    val fileId: String,
    /** Unique identifier for this file, which is supposed to be the same over time and for different bots. Can't be used to download or reuse the file. */
    @SerializedName("file_unique_id")
    val fileUniqueId: String,
    /** Photo width */
    val width: Int,
    /** Photo height */
    val height: Int,
    /** File size in bytes */
    @SerializedName("file_size")
    val fileSize: Int?,
)
