package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

import com.google.gson.annotations.SerializedName

data class TgGetChatMemberCountRequest(
    @SerializedName("chat_id")
    val chatId: Long,
)
