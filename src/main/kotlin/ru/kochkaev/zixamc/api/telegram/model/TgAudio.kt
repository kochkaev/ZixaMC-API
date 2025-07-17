package ru.kochkaev.zixamc.api.telegram.model

/** This object represents an audio file to be treated as music by the Telegram clients. */
class TgAudio(
    fileId: String,
    fileUniqueId: String,
    /** Duration of the audio in seconds as defined by the sender */
    val duration: Int?,
    /** Performer of the audio as defined by the sender or by audio tags */
    val performer: String?,
    /** Title of the audio as defined by the sender or by audio tags */
    val title: String?,
    fileName: String?,
    mimeType: String?,
    fileSize: Long?,
    /** Thumbnail of the album cover to which the music file belongs */
    thumbnail: TgPhotoSize?,
): TgDocument(fileId, fileUniqueId, thumbnail, fileName, mimeType, fileSize)
