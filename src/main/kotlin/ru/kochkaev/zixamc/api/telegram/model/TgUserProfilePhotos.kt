package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represent a user's profile pictures. */
data class TgUserProfilePhotos(
    /** Total number of profile pictures the target user has */
    @SerializedName("total_count")
    val totalCount: Int,
    /** Requested profile pictures (in up to 4 sizes each) */
    val photos: List<TgPhotoSize>,
)
