package ru.kochkaev.zixamc.tgbridge.sql.data

import com.google.gson.annotations.SerializedName

data class NewProtectedData(
    @SerializedName("message_id")
    val messageId: Int,
    @SerializedName("protected_type")
    val protectedType: ProtectedType,
    @SerializedName("sender_bot_id")
    val senderBotId: Long,
) {
    enum class ProtectedType {
        @SerializedName("text")
        TEXT,
        @SerializedName("reply_markup")
        REPLY_MARKUP,
    }
}