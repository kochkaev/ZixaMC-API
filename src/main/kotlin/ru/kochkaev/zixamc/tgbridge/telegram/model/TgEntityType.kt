package ru.kochkaev.zixamc.tgbridge.telegram.model

import com.google.gson.annotations.SerializedName

enum class TgEntityType {
    @SerializedName("bold")
    BOLD,
    @SerializedName("italic")
    ITALIC,
    @SerializedName("underline")
    UNDERLINE,
    @SerializedName("strikethrough")
    STRIKETHROUGH,
    @SerializedName("text_link")
    TEXT_LINK,
    @SerializedName("url")
    URL,
    @SerializedName("mention")
    MENTION,
    @SerializedName("hashtag")
    HASHTAG,
    @SerializedName("cashtag")
    CASHTAG,
    @SerializedName("spoiler")
    SPOILER,
    @SerializedName("code")
    CODE,
    @SerializedName("pre")
    PRE,
}