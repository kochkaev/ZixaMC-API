package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
data class TgExternalReplyInfo(
    val origin: TgMessageOrigin,
    val chat: TgChat? = null,
    override val animation: TgAny? = null,
    override val photo: List<TgAny>? = null,
    override val audio: TgAudio? = null,
    override val document: TgDocument? = null,
    override val sticker: TgAny? = null,
    override val video: TgAny? = null,
    @SerializedName("video_note")
    override val videoNote: TgAny? = null,
    override val voice: TgAny? = null,
    override val poll: TgPoll? = null,
) : TgMessageMedia {
    val senderName
        get() = origin.senderUser?.let {
            (it.firstName + " " + (it.lastName ?: "")).trim()
        }
            ?: origin.senderUserName
            ?: origin.senderChat?.title
            ?: origin.chat?.title
            ?: ""
}
