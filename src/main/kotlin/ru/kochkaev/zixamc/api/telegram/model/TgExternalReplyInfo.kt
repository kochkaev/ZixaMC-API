package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object contains information about a message that is being replied to, which may come from another chat or forum topic. */
data class TgExternalReplyInfo(
    /** Origin of the message replied to by the given message */
    val origin: TgMessageOrigin,
    /** Chat the original message belongs to. Available only if the chat is a supergroup or a channel. */
    val chat: TgChat? = null,
    /** Unique message identifier inside the original chat. Available only if the original chat is a supergroup or a channel. */
    @SerializedName("message_id")
    val messageId: Int? = null,
    /** Options used for link preview generation for the original message, if it is a text message */
    @SerializedName("link_preview_options")
    val linkPreviewOptions: TgLinkPreviewOptions? = null,
    /** Message is an animation, information about the animation */
    val animation: TgAnimation? = null,
    /** Message is an audio file, information about the file */
    val audio: TgAudio? = null,
    /** Message is a general file, information about the file */
    val document: TgDocument? = null,
    /** Message is a photo, available sizes of the photo */
    val photo: List<TgPhotoSize>? = null,
    /** Message is a sticker, information about the sticker */
    val sticker: TgSticker? = null,
    /** Message is a forwarded story */
    val story: TgStory? = null,
    /** Message is a video, information about the video */
    val video: TgVideo? = null,
    /** Message is a video note, information about the video message */
    @SerializedName("video_note")
    val videoNote: TgVideoNote? = null,
    /** Message is a voice message, information about the file */
    val voice: TgVoice? = null,
    /** True, if the message media is covered by a spoiler animation */
    @SerializedName("has_media_spoiler")
    val hasMediaSpoiler: Boolean? = null,
    /** Message is a checklist */
    val checklist: TgChecklist? = null,
    /** Message is a shared contact, information about the contact */
    val contact: TgContact? = null,
    /** Message is a dice with random value */
    val dice: TgDice? = null,
    /** Message is a game, information about the game. More about games: https://core.telegram.org/bots/api#games */
    val game: TgGame? = null,
    /** Message is a scheduled giveaway, information about the giveaway */
    val giveaway: TgGiveaway? = null,
    /** A giveaway with public winners was completed */
    @SerializedName("giveaway_winners")
    val giveawayWinners: TgGiveawayWinners? = null,
    /** Message is an invoice for a payment, information about the invoice. More about payments: https://core.telegram.org/bots/api#payments */
    val invoice: TgInvoice? = null,
    /** Message is a shared location, information about the location */
    val location: TgLocation? = null,
    /** Message is a native poll, information about the poll */
    val poll: TgPoll? = null,
    /** Message is a venue, information about the venue */
    val venue: TgVenue? = null,
) {
    val senderName
        get() = (when (origin) {
            is TgMessageOriginUser -> origin.senderUser.firstName + " " + (origin.senderUser.lastName ?: "").trim()
            is TgMessageOriginHiddenUser -> origin.senderUserName
            is TgMessageOriginChat -> origin.senderChat.title
            is TgMessageOriginChannel -> origin.chat.title
            else -> null
        })?: ""
}
