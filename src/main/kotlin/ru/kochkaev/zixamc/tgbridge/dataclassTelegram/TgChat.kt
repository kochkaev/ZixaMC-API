package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
data class TgChat(
    val id: Long,
    val title: String = "",
    val username: String? = null,
    @SerializedName("is_forum")
    val isForum: Boolean = false,
)
