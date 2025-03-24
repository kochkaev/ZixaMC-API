package ru.kochkaev.zixamc.tgbridge.telegram.model

/**
 * @author vanutp
 */
interface TgMessageMedia {
    val animation: TgAny?
    val photo: List<TgAny>?
    val audio: TgAudio?
    val document: TgDocument?
    val sticker: TgAny?
    val video: TgAny?
    val videoNote: TgAny?
    val voice: TgAny?
    val poll: TgPoll?
}