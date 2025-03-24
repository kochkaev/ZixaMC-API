package ru.kochkaev.zixamc.tgbridge.telegram.model

import com.google.gson.annotations.SerializedName

enum class TgChatType {
    @SerializedName("private")
    PRIVATE,
    @SerializedName("group")
    GROUP,
    @SerializedName("supergroup")
    SUPERGROUP,
    @SerializedName("channel")
    CHANNEL,
}