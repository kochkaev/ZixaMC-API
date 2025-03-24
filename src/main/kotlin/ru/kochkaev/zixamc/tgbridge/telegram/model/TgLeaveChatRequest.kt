package ru.kochkaev.zixamc.tgbridge.telegram.model

import com.google.gson.annotations.SerializedName

data class TgLeaveChatRequest(
    @SerializedName("chat_id")
    val chatId: Long
)
