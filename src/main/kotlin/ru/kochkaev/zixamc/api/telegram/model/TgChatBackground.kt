package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.serialize.TgBackgroundFillAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgBackgroundTypeAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTypeEnum

/** This object represents a chat background. */
data class TgChatBackground(
    /** Type of the background */
    val type: TgBackgroundType,
)
/** This object describes the type of a background. Currently, it can be one of BackgroundTypeFill, BackgroundTypeWallpaper, BackgroundTypePattern or BackgroundTypeChatTheme */
@JsonAdapter(TgBackgroundTypeAdapter::class)
open class TgBackgroundType(
    /** Type of the background */
    val type: TgBackgroundTypes,
)
enum class TgBackgroundTypes: TgTypeEnum {
    @SerializedName("fill")
    FILL {
        override val model = TgBackgroundTypeFill::class.java
    },
    @SerializedName("wallpaper")
    WALLPAPER {
        override val model = TgBackgroundTypeWallpaper::class.java
    },
    @SerializedName("pattern")
    PATTERN {
        override val model = TgBackgroundTypePattern::class.java
    },
    @SerializedName("chat_theme")
    CHAT_THEME {
        override val model = TgBackgroundTypeChatTheme::class.java
    },
}
/** The background is automatically filled based on the selected colors. */
data class TgBackgroundTypeFill(
    /** The background fill */
    val fill: TgBackgroundFill,
    /** Dimming of the background in dark themes, as a percentage; 0-100 */
    @SerializedName("dark_theme_dimming")
    val darkThemeDimming: Int,
): TgBackgroundType(TgBackgroundTypes.FILL)
/** The background is a wallpaper in the JPEG format. */
data class TgBackgroundTypeWallpaper(
    /** Document with the wallpaper */
    val document: TgDocument,
    /** Dimming of the background in dark themes, as a percentage; 0-100 */
    @SerializedName("dark_theme_dimming")
    val darkThemeDimming: Int,
    /** True, if the wallpaper is downscaled to fit in a 450x450 square and then box-blurred with radius 12 */
    @SerializedName("is_blurred")
    val isBlurred: Boolean?,
    /** True, if the background moves slightly when the device is tilted */
    @SerializedName("is_moving")
    val isMoving: Boolean?,
): TgBackgroundType(TgBackgroundTypes.WALLPAPER)
/** The background is a wallpaper in the JPEG format. */
data class TgBackgroundTypePattern(
    /** Document with the wallpaper */
    val document: TgDocument,
    /** The background fill that is combined with the pattern */
    val fill: TgBackgroundFill,
    /** Intensity of the pattern when it is shown above the filled background; 0-100 */
    val intensity: Int,
    /** True, if the background fill must be applied only to the pattern itself. All other pixels are black in this case. For dark themes only */
    @SerializedName("is_inverted")
    val isInverted: Boolean?,
    /** True, if the background moves slightly when the device is tilted */
    @SerializedName("is_moving")
    val isMoving: Boolean?,
): TgBackgroundType(TgBackgroundTypes.PATTERN)
/** The background is a wallpaper in the JPEG format. */
data class TgBackgroundTypeChatTheme(
    /** Name of the chat theme, which is usually an emoji */
    @SerializedName("theme_name")
    val themeName: String,
): TgBackgroundType(TgBackgroundTypes.CHAT_THEME)

/** This object describes the way a background is filled based on the selected colors. Currently, it can be one of BackgroundFillSolid, BackgroundFillGradient or BackgroundFillFreeformGradient */
@JsonAdapter(TgBackgroundFillAdapter::class)
open class TgBackgroundFill(
    /** Type of the background fill */
    val type: TgBackgroundFillType,
)
enum class TgBackgroundFillType: TgTypeEnum {
    @SerializedName("solid")
    SOLID {
        override val model = TgBackgroundFillSolid::class.java
    },
    @SerializedName("gradient")
    GRADIENT {
        override val model = TgBackgroundFillGradient::class.java
    },
    @SerializedName("freedom_gradient")
    FREEDOM_GRADIENT {
        override val model = TgBackgroundFillFreeformGradient::class.java
    },
}
/** The background is filled using the selected color. */
data class TgBackgroundFillSolid(
    /** The color of the background fill in the RGB24 format */
    val color: Int,
): TgBackgroundFill(TgBackgroundFillType.SOLID)
/** The background is a gradient fill. */
data class TgBackgroundFillGradient(
    /** Top color of the gradient in the RGB24 format */
    @SerializedName("top_color")
    val topColor: Int,
    /** Bottom color of the gradient in the RGB24 format */
    @SerializedName("bottom_color")
    val bottomColor: Int,
    /** Clockwise rotation angle of the background fill in degrees; 0-359 */
    @SerializedName("rotation_angle")
    val rotationAngle: Int,
): TgBackgroundFill(TgBackgroundFillType.GRADIENT)
/** The background is a gradient fill. */
data class TgBackgroundFillFreeformGradient(
    /** A list of the 3 or 4 base colors that are used to generate the freeform gradient in the RGB24 format */
    val colors: List<Int>,
): TgBackgroundFill(TgBackgroundFillType.FREEDOM_GRADIENT)