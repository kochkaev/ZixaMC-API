package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

data class TgLeaveChatRequest(
    @SerializedName("chat_id")
    val chatId: Long
)
