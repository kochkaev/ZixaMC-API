package ru.kochkaev.zixamc.requests.dataclassTelegram

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
data class TgMessageOrigin(
    @SerializedName("sender_user")
    val senderUser: TgUser? = null,
    @SerializedName("sender_user_name")
    val senderUserName: String? = null,
    @SerializedName("sender_chat")
    val senderChat: TgChat? = null,
    val chat: TgChat? = null,
)