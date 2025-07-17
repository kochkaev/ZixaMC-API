package ru.kochkaev.zixamc.api.telegram.model

/**
 * @author vanutp
 */
interface TgMessageMedia {
    val animation: TgAnimation?
    val photo: List<TgPhotoSize>?
    val audio: TgAudio?
    val document: TgDocument?
    val sticker: TgSticker?
    val video: TgVideo?
    val videoNote: TgVideoNote?
    val voice: TgVoice?
    val poll: TgPoll?
}