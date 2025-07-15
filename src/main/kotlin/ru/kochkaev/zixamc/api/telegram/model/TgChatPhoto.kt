package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a chat photo. */
data class TgChatPhoto(
    /** File identifier of small (160x160) chat photo. This file_id can be used only for photo download and only for as long as the photo is not changed. */
    @SerializedName("small_file_id")
    val smallFileId: String,
    /** Unique file identifier of small (160x160) chat photo, which is supposed to be the same over time and for different bots. Can't be used to download or reuse the file. */
    @SerializedName("small_file_unique_id")
    val smallFileUniqueId: String,
    /** File identifier of big (640x640) chat photo. This file_id can be used only for photo download and only for as long as the photo is not changed. */
    @SerializedName("big_file_id")
    val bigFileId: String,
    /** Unique file identifier of big (640x640) chat photo, which is supposed to be the same over time and for different bots. Can't be used to download or reuse the file. */
    @SerializedName("big_file_unique_id")
    val bigFileUniqueId: String,
)
