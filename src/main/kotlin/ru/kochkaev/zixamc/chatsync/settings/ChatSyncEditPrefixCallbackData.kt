package ru.kochkaev.zixamc.chatsync.settings

import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.callback.CallbackData
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.api.telegram.model.TgCallbackQuery

data class ChatSyncEditPrefixCallbackData(
    val name: String?
): CallbackData {
    companion object {
        suspend fun onCallback(
            cbq: TgCallbackQuery,
            sql: SQLCallback<ChatSyncEditPrefixCallbackData>
        ): TgCBHandlerResult {
            if (sql.data == null) return TgCBHandlerResult.SUCCESS
            val group = SQLGroup.get(cbq.message.chat.id) ?: return TgCBHandlerResult.SUCCESS
            if (sql.data!!.name == null) return TgCBHandlerResult.SUCCESS
//                val type = FeatureTypes.entries[sql.data!!.name!!] ?: return SUCCESS
            ChatSyncFeatureType.sendNeedPrefix(
                group = group,
                replyTo = cbq.message.messageId,
                topicId = null,
                prefixType = GroupChatSyncWaitPrefixProcessData.PrefixTypes.valueOf(sql.data!!.name!!)
            )
            return TgCBHandlerResult.DELETE_MARKUP
        }
    }
}
