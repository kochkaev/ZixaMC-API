package ru.kochkaev.zixamc.tgbridge.telegram.model

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
class TgChatFullInfo(
    id: Long,
    type: TgChatType,
    title: String = "",
    username: String? = null,
    isForum: Boolean = false,
): TgChat(id, type, title, username, isForum)
