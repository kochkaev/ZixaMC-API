package ru.kochkaev.zixamc.tgbridge.telegram.model

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
open class TgChat(
    open val id: Long,
    open val type: TgChatType,
    open val title: String = "",
    open val username: String? = null,
    @SerializedName("is_forum")
    open val isForum: Boolean = false,
)
