package ru.kochkaev.zixamc.api.telegram.model

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
    @SerializedName("text_mention")
    TEXT_MENTION,
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
    @SerializedName("blockquote")
    BLOCKQUOTE,
    @SerializedName("expandable_blockquote")
    EXPANDABLE_BLOCKQUOTE,
    @SerializedName("bot_command")
    BOT_COMMAND,
    @SerializedName("email")
    EMAIL,
    @SerializedName("phone_number")
    PRONE_NUMBER,
    @SerializedName("custom_emoji")
    CUSTOM_EMOJI,
}