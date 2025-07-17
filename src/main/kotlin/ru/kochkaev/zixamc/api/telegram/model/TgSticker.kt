package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a sticker. */
data class TgSticker(
    /** Identifier for this file, which can be used to download or reuse the file */
    @SerializedName("file_id")
    val fileId: String,
    /** Unique identifier for this file, which is supposed to be the same over time and for different bots. Can't be used to download or reuse the file. */
    @SerializedName("file_unique_id")
    val fileUniqueId: String,
    /** Type of the sticker, currently one of “regular”, “mask”, “custom_emoji”. The type of the sticker is independent from its format, which is determined by the fields is_animated and is_video. */
    val type: TgStickerType,
    /** Sticker width */
    val width: Int,
    /** Sticker height */
    val height: Int,
    /** True, if the sticker is animated */
    @SerializedName("is_animated")
    val isAnimated: Boolean,
    /** True, if the sticker is a video sticker */
    @SerializedName("is_video")
    val isVideo: Boolean,
    /** Sticker thumbnail in the .WEBP or .JPG format */
    val thumbnail: TgPhotoSize?,
    /** Emoji associated with the sticker */
    val emoji: String?,
    /** Name of the sticker set to which the sticker belongs */
    @SerializedName("set_name")
    val setName: String?,
    /** For premium regular stickers, premium animation for the sticker */
    @SerializedName("premium_animation")
    val premiumAnimation: TgFile?,
    /** For mask stickers, the position where the mask should be placed */
    @SerializedName("mask_position")
    val maskPosition: TgMaskPosition?,
    /** For custom emoji stickers, unique identifier of the custom emoji */
    @SerializedName("custom_emoji_id")
    val customEmojiId: TgMaskPosition?,
    /** True, if the sticker must be repainted to a text color in messages, the color of the Telegram Premium badge in emoji status, white color on chat photos, or another appropriate color in other places */
    @SerializedName("needs_repainting")
    val needsRepainting: TgMaskPosition?,
    /** File size in bytes */
    @SerializedName("file_size")
    val fileSize: Int?,
)
enum class TgStickerType {
    @SerializedName("regular")
    REGULAR,
    @SerializedName("mask")
    MASK,
    @SerializedName("custom_emoji")
    CUSTOM_EMOJI,
}
