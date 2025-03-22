package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

import com.google.gson.annotations.SerializedName

data class TgGetChatMemberRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("user_id")
    val userId: Long
)
