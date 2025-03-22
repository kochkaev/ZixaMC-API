package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

import com.google.gson.annotations.SerializedName

data class TgChatMemberUpdated(
    val chat: TgChat,
    val from: TgUser,
    @SerializedName("old_chat_member")
    val oldChatMember: TgChatMember,
    @SerializedName("new_chat_member")
    val newChatMember: TgChatMember,
)
