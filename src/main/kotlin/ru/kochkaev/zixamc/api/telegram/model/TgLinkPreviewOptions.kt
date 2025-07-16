package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** Describes the options used for link preview generation. */
data class TgLinkPreviewOptions(
    /** True, if the link preview is disabled */
    @SerializedName("is_disabled")
    val isDisabled: Boolean?,
    /** URL to use for the link preview. If empty, then the first URL found in the message text will be used */
    val url: String?,
    /** True, if the media in the link preview is supposed to be shrunk; ignored if the URL isn't explicitly specified or media size change isn't supported for the preview */
    @SerializedName("prefer_small_media")
    val preferSmallMedia: Boolean?,
    /** True, if the media in the link preview is supposed to be enlarged; ignored if the URL isn't explicitly specified or media size change isn't supported for the preview */
    @SerializedName("prefer_large_media")
    val preferLargeMedia: Boolean?,
    /** True, if the link preview must be shown above the message text; otherwise, the link preview will be shown below the message text */
    @SerializedName("show_above_text")
    val showAboveText: Boolean?,
)
