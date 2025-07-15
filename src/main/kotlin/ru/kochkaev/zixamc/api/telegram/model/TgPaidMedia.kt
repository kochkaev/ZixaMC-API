package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.serialize.TgPaidMediaAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTypeEnum

/** This object describes paid media. Currently, it can be one of PaidMediaPreview, PaidMediaPhoto or PaidMediaVideo */
@JsonAdapter(TgPaidMediaAdapter::class)
open class TgPaidMedia(
    val type: TgPaidMediaType
)
enum class TgPaidMediaType: TgTypeEnum {
    @SerializedName("preview")
    PREVIEW {
        override val model = TgPaidMediaPreview::class.java
    },
    @SerializedName("photo")
    PHOTO {
        override val model = TgPaidMediaPhoto::class.java
    },
    @SerializedName("video")
    VIDEO {
        override val model = TgPaidMediaVideo::class.java
    },
}

/** The paid media isn't available before the payment. */
class TgPaidMediaPreview(
    /** Media width as defined by the sender */
    val width: Int?,
    /** Media height as defined by the sender */
    val height: Int?,
    /** Duration of the media in seconds as defined by the sender */
    val duration: Int?,
): TgPaidMedia(TgPaidMediaType.PREVIEW)
/** The paid media is a photo. */
class TgPaidMediaPhoto(
    /** The photo */
    val photo: List<TgPhotoSize>,
): TgPaidMedia(TgPaidMediaType.PHOTO)
/** The paid media is a video. */
class TgPaidMediaVideo(
    /** The video */
    val video: TgVideo,
): TgPaidMedia(TgPaidMediaType.VIDEO)