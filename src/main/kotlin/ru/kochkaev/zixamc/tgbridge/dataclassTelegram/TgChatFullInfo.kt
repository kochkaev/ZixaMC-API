package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
open class TgChatFullInfo(
    override val id: Long,
    override val title: String = "",
    override val username: String? = null,
    @SerializedName("is_forum")
    override val isForum: Boolean = false,
): TgChat(id, title, username, isForum)
