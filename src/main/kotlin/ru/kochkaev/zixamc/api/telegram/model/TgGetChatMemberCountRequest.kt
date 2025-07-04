package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

data class TgGetChatMemberCountRequest(
    @SerializedName("chat_id")
    val chatId: Long,
)
