package ru.kochkaev.zixamc.api.telegram.model

/** This object represents an animation file (GIF or H.264/MPEG-4 AVC video without sound). */
class TgAnimation(
    fileId: String,
    fileUniqueId: String,
    /** Type of the sticker, currently one of “regular”, “mask”, “custom_emoji”. The type of the sticker is independent from its format, which is determined by the fields is_animated and is_video. */
    val type: TgStickerType,
    /** Video weight as defined by the sender */
    val width: Int,
    /** Video height as defined by the sender */
    val height: Int,
    /** Duration of the video in seconds as defined by the sender */
    val duration: Int,
    /** Animation thumbnail as defined by the sender */
    thumbnail: TgPhotoSize?,
    fileName: String?,
    mimeType: String?,
    fileSize: Long?,
): TgDocument(fileId, fileUniqueId, thumbnail, fileName, mimeType, fileSize)
