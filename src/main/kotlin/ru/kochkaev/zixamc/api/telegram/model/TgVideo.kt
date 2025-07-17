package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a video file. */
class TgVideo(
    fileId: String,
    fileUniqueId: String,
    /** Video width as defined by the sender */
    val width: Int,
    /** Video height as defined by the sender */
    val height: Int,
    /** Duration of the video in seconds as defined by the sender */
    val duration: Int,
    /** Video thumbnail */
    thumbnail: TgPhotoSize?,
    /** Available sizes of the cover of the video in the message */
    val cover: List<TgPhotoSize>?,
    /** Timestamp in seconds from which the video will play in the message */
    @SerializedName("start_timestamp")
    val startTimestamp: Int?,
    fileName: String?,
    mimeType: String?,
    fileSize: Long?,
): TgDocument(fileId, fileUniqueId, thumbnail, fileName, mimeType, fileSize)
