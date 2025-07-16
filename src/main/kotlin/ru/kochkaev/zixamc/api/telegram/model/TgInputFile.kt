package ru.kochkaev.zixamc.api.telegram.model
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.serialize.TgInputMediaAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgInputPaidMediaAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgInputProfilePhotoAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgInputStoryContentAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTypeEnum

/** This object represents the contents of a file to be uploaded. Must be posted using multipart/form-data in the usual way that files are uploaded via the browser. */
typealias TgInputFile = Any

/** This object represents the content of a media message to be sent. It should be one of InputMediaAnimation, InputMediaDocument, InputMediaAudio, InputMediaPhoto or InputMediaVideo */
@JsonAdapter(TgInputMediaAdapter::class)
open class TgInputMedia(
    /** Type of the result */
    val type: TgInputMediaType,
    /** File to send. Pass a file_id to send a file that exists on the Telegram servers (recommended), pass an HTTP URL for Telegram to get a file from the Internet, or pass “attach://&ltfile_attach_name&gt” to upload a new one using multipart/form-data under &ltfile_attach_name&gt name. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val media: String,
    /** Caption of the document to be sent, 0-1024 characters after entities parsing */
    val caption: String? = null,
    /** Mode for parsing entities in the document caption. See formatting options for more details. */
    @SerializedName("parse_mode")
    val parseMode: String? = "HTML",
    /** List of special entities that appear in the caption, which can be specified instead of parse_mode */
    @SerializedName("caption_entities")
    val captionEntities: List<TgMessageEntity>? = null,
)
enum class TgInputMediaType: TgTypeEnum {
    @SerializedName("photo")
    PHOTO {
        override val model = TgInputMediaPhoto::class.java
    },
    @SerializedName("video")
    VIDEO {
        override val model = TgInputMediaVideo::class.java
    },
    @SerializedName("animation")
    ANIMATION {
        override val model = TgInputMediaAnimation::class.java
    },
    @SerializedName("audio")
    AUDIO {
        override val model = TgInputMediaAudio::class.java
    },
    @SerializedName("document")
    DOCUMENT {
        override val model = TgInputMediaDocument::class.java
    },
}
/** Represents a photo to be sent. */
class TgInputMediaPhoto(
    media: String,
    caption: String? = null,
    parseMode: String? = null,
    captionEntities: List<TgMessageEntity>? = null,
    /** Pass True, if the caption must be shown above the message media */
    @SerializedName("show_caption_above_media")
    val showCaptionAboveMedia: Boolean? = null,
    /** Pass True if the photo needs to be covered with a spoiler animation */
    @SerializedName("has_spoiler")
    val hasSpoiler: Boolean? = null,
): TgInputMedia(TgInputMediaType.PHOTO, media, caption, parseMode, captionEntities)
/** Represents a video to be sent. */
class TgInputMediaVideo(
    media: String,
    /** Thumbnail of the file sent; can be ignored if thumbnail generation for the file is supported server-side. The thumbnail should be in JPEG format and less than 200 kB in size. A thumbnail's width and height should not exceed 320. Ignored if the file is not uploaded using multipart/form-data. Thumbnails can't be reused and can be only uploaded as a new file, so you can pass “attach://&ltfile_attach_name&gt” if the thumbnail was uploaded using multipart/form-data under &ltfile_attach_name&gt. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val thumbnail: String,
    /** Cover for the video in the message. Pass a file_id to send a file that exists on the Telegram servers (recommended), pass an HTTP URL for Telegram to get a file from the Internet, or pass “attach://&ltfile_attach_name&gt” to upload a new one using multipart/form-data under &ltfile_attach_name&gt name. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val cover: String,
    /** Start timestamp for the video in the message */
    @SerializedName("start_timestamp")
    val startTimestamp: Int? = null,
    caption: String? = null,
    parseMode: String? = null,
    captionEntities: List<TgMessageEntity>? = null,
    /** Pass True, if the caption must be shown above the message media */
    @SerializedName("show_caption_above_media")
    val showCaptionAboveMedia: Boolean? = null,
    /** Video width */
    val width: Int? = null,
    /** Video height */
    val height: Int? = null,
    /** Video duration in seconds */
    val duration: Int? = null,
    /** Pass True if the uploaded video is suitable for streaming */
    @SerializedName("supports_streaming")
    val supportsStreaming: Boolean? = null,
    /** Pass True if the video needs to be covered with a spoiler animation */
    @SerializedName("has_spoiler")
    val hasSpoiler: Boolean? = null,
): TgInputMedia(TgInputMediaType.VIDEO, media, caption, parseMode, captionEntities)
/** Represents an animation file (GIF or H.264/MPEG-4 AVC video without sound) to be sent. */
class TgInputMediaAnimation(
    media: String,
    /** Thumbnail of the file sent; can be ignored if thumbnail generation for the file is supported server-side. The thumbnail should be in JPEG format and less than 200 kB in size. A thumbnail's width and height should not exceed 320. Ignored if the file is not uploaded using multipart/form-data. Thumbnails can't be reused and can be only uploaded as a new file, so you can pass “attach://&ltfile_attach_name&gt” if the thumbnail was uploaded using multipart/form-data under &ltfile_attach_name&gt. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val thumbnail: String,
    caption: String? = null,
    parseMode: String? = null,
    captionEntities: List<TgMessageEntity>? = null,
    /** Pass True, if the caption must be shown above the message media */
    @SerializedName("show_caption_above_media")
    val showCaptionAboveMedia: Boolean? = null,
    /** Animation width */
    val width: Int? = null,
    /** Animation height */
    val height: Int? = null,
    /** Animation duration in seconds */
    val duration: Int? = null,
    /** Pass True if the animation needs to be covered with a spoiler animation */
    @SerializedName("has_spoiler")
    val hasSpoiler: Boolean? = null,
): TgInputMedia(TgInputMediaType.ANIMATION, media, caption, parseMode, captionEntities)
/** Represents an audio file to be treated as music to be sent. */
class TgInputMediaAudio(
    media: String,
    /** Thumbnail of the file sent; can be ignored if thumbnail generation for the file is supported server-side. The thumbnail should be in JPEG format and less than 200 kB in size. A thumbnail's width and height should not exceed 320. Ignored if the file is not uploaded using multipart/form-data. Thumbnails can't be reused and can be only uploaded as a new file, so you can pass “attach://&ltfile_attach_name&gt” if the thumbnail was uploaded using multipart/form-data under &ltfile_attach_name&gt. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val thumbnail: String,
    caption: String? = null,
    parseMode: String? = null,
    captionEntities: List<TgMessageEntity>? = null,
    /** Duration of the audio in seconds */
    val duration: Int? = null,
    /** Performer of the audio */
    val performer: String? = null,
    /** Title of the audio */
    val title: String? = null,
): TgInputMedia(TgInputMediaType.AUDIO, media, caption, parseMode, captionEntities)
/**  */
class TgInputMediaDocument(
    media: String,
    /** Thumbnail of the file sent; can be ignored if thumbnail generation for the file is supported server-side. The thumbnail should be in JPEG format and less than 200 kB in size. A thumbnail's width and height should not exceed 320. Ignored if the file is not uploaded using multipart/form-data. Thumbnails can't be reused and can be only uploaded as a new file, so you can pass “attach://&ltfile_attach_name&gt” if the thumbnail was uploaded using multipart/form-data under &ltfile_attach_name&gt. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val thumbnail: String,
    caption: String? = null,
    parseMode: String? = null,
    captionEntities: List<TgMessageEntity>? = null,
    /** Disables automatic server-side content type detection for files uploaded using multipart/form-data. Always True, if the document is sent as part of an album. */
    @SerializedName("disable_content_type_detection")
    val disableContentTypeDetection: Boolean? = null,
): TgInputMedia(TgInputMediaType.DOCUMENT, media, caption, parseMode, captionEntities)

/** This object describes the paid media to be sent. Currently, it can be one of InputPaidMediaPhoto or InputPaidMediaVideo */
@JsonAdapter(TgInputPaidMediaAdapter::class)
open class TgInputPaidMedia(
    /** Type of the result */
    val type: TgInputPaidMediaType,
    /** File to send. Pass a file_id to send a file that exists on the Telegram servers (recommended), pass an HTTP URL for Telegram to get a file from the Internet, or pass “attach://&ltfile_attach_name&gt” to upload a new one using multipart/form-data under &ltfile_attach_name&gt name. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val media: String,
)
enum class TgInputPaidMediaType: TgTypeEnum {
    @SerializedName("photo")
    PHOTO {
        override val model = TgInputPaidMediaPhoto::class.java
    },
    @SerializedName("video")
    VIDEO {
        override val model = TgInputPaidMediaVideo::class.java
    },
}
/** Represents a photo to be sent. */
class TgInputPaidMediaPhoto(
    media: String,
): TgInputPaidMedia(TgInputPaidMediaType.PHOTO, media)
/** Represents a video to be sent. */
class TgInputPaidMediaVideo(
    media: String,
    /** Thumbnail of the file sent; can be ignored if thumbnail generation for the file is supported server-side. The thumbnail should be in JPEG format and less than 200 kB in size. A thumbnail's width and height should not exceed 320. Ignored if the file is not uploaded using multipart/form-data. Thumbnails can't be reused and can be only uploaded as a new file, so you can pass “attach://&ltfile_attach_name&gt” if the thumbnail was uploaded using multipart/form-data under &ltfile_attach_name&gt. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val thumbnail: String,
    /** Cover for the video in the message. Pass a file_id to send a file that exists on the Telegram servers (recommended), pass an HTTP URL for Telegram to get a file from the Internet, or pass “attach://&ltfile_attach_name&gt” to upload a new one using multipart/form-data under &ltfile_attach_name&gt name. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val cover: String,
    /** Start timestamp for the video in the message */
    @SerializedName("start_timestamp")
    val startTimestamp: Int? = null,
    /** Pass True, if the caption must be shown above the message media */
    @SerializedName("show_caption_above_media")
    val showCaptionAboveMedia: Boolean? = null,
    /** Video width */
    val width: Int? = null,
    /** Video height */
    val height: Int? = null,
    /** Video duration in seconds */
    val duration: Int? = null,
    /** Pass True if the uploaded video is suitable for streaming */
    @SerializedName("supports_streaming")
    val supportsStreaming: Boolean? = null,
): TgInputPaidMedia(TgInputPaidMediaType.VIDEO, media)

/** This object describes a profile photo to set. Currently, it can be one of InputProfilePhotoStatic or InputProfilePhotoAnimated */
@JsonAdapter(TgInputProfilePhotoAdapter::class)
open class TgInputProfilePhoto(
    /** Type of the profile photo */
    val type: TgInputProfilePhotoType,
)
enum class TgInputProfilePhotoType: TgTypeEnum {
    @SerializedName("static")
    STATIC {
        override val model = TgInputProfilePhotoStatic::class.java
    },
    @SerializedName("animated")
    ANIMATED {
        override val model = TgInputProfilePhotoAnimated::class.java
    },
}
/** A static profile photo in the .JPG format. */
class TgInputProfilePhotoStatic(
    /** The static profile photo. Profile photos can't be reused and can only be uploaded as a new file, so you can pass “attach://&ltfile_attach_name&gt” if the photo was uploaded using multipart/form-data under &ltfile_attach_name&gt. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val photo: String,
): TgInputProfilePhoto(TgInputProfilePhotoType.STATIC)
/** An animated profile photo in the MPEG4 format. */
class TgInputProfilePhotoAnimated(
    /** The animated profile photo. Profile photos can't be reused and can only be uploaded as a new file, so you can pass “attach://&ltfile_attach_name&gt” if the photo was uploaded using multipart/form-data under &ltfile_attach_name&gt. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val animation: String,
    /** Timestamp in seconds of the frame that will be used as the static profile photo. Defaults to 0.0. */
    @SerializedName("main_frame_timestamp")
    val mainFrameTimestamp: Float? = null,
): TgInputProfilePhoto(TgInputProfilePhotoType.ANIMATED)

/** This object describes the content of a story to post. Currently, it can be one of InputStoryContentPhoto or InputStoryContentVideo */
@JsonAdapter(TgInputStoryContentAdapter::class)
open class TgInputStoryContent(
    /** Type of the profile photo */
    val type: TgInputStoryContentType,
)
enum class TgInputStoryContentType: TgTypeEnum {
    @SerializedName("photo")
    PHOTO {
        override val model = TgInputStoryContentPhoto::class.java
    },
    @SerializedName("video")
    VIDEO {
        override val model = TgInputStoryContentVideo::class.java
    },
}
/** Describes a photo to post as a story. */
class TgInputStoryContentPhoto(
    /** The photo to post as a story. The photo must be of the size 1080x1920 and must not exceed 10 MB. The photo can't be reused and can only be uploaded as a new file, so you can pass “attach://&ltfile_attach_name&gt” if the photo was uploaded using multipart/form-data under &ltfile_attach_name&gt. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val photo: String,
): TgInputStoryContent(TgInputStoryContentType.PHOTO)
/** Describes a video to post as a story. */
class TgInputStoryContentVideo(
    /** The video to post as a story. The video must be of the size 720x1280, streamable, encoded with H.265 codec, with key frames added each second in the MPEG4 format, and must not exceed 30 MB. The video can't be reused and can only be uploaded as a new file, so you can pass “attach://&ltfile_attach_name&gt” if the photo was uploaded using multipart/form-data under &ltfile_attach_name&gt. More information on Sending Files: https://core.telegram.org/bots/api#sending-files */
    val video: String,
    /** Precise duration of the video in seconds; 0-60 */
    val duration: Float? = null,
    /** Timestamp in seconds of the frame that will be used as the static cover for the story. Defaults to 0.0. */
    @SerializedName("cover_frame_timestamp")
    val coverFrameTimestamp: Float? = null,
    /** Pass True if the video has no sound */
    @SerializedName("is_animation")
    val isAnimation: Boolean? = null,
): TgInputStoryContent(TgInputStoryContentType.VIDEO)