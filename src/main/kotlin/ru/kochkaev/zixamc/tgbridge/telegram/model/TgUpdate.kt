package ru.kochkaev.zixamc.tgbridge.telegram.model

import com.google.gson.annotations.SerializedName

/**
 * @author vanutp
 */
data class TgUpdate(
    @SerializedName("update_id")
    val updateId: Int,
    val message: TgMessage? = null,
    @SerializedName("callback_query")
    val callbackQuery: TgCallbackQuery? = null,
    @SerializedName("chat_join_request")
    val chatJoinRequest: TgChatJoinRequest? = null,
    @SerializedName("my_chat_member")
    val myChatMember: TgChatMemberUpdated? = null,
    @SerializedName("chat_member")
    val chatMember: TgChatMemberUpdated? = null,
)
