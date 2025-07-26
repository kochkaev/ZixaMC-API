package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object describes the position on faces where a mask should be placed by default. */
data class TgMaskPosition(
    /** The part of the face relative to which the mask should be placed. One of “forehead”, “eyes”, “mouth”, or “chin”. */
    val point: String,
    /** Shift by X-axis measured in widths of the mask scaled to the face size, from left to right. For example, choosing -1.0 will place mask just to the left of the default mask position. */
    @SerializedName("x_shift")
    val xShift: Float,
    /** Shift by Y-axis measured in heights of the mask scaled to the face size, from top to bottom. For example, 1.0 will place the mask just below the default mask position. */
    @SerializedName("y_shift")
    val yShift: Float,
    /** Mask scaling coefficient. For example, 2.0 means double size. */
    val scale: Float,
)
