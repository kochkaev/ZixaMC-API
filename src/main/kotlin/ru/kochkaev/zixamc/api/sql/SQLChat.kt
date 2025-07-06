package ru.kochkaev.zixamc.api.sql

import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataTypes
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.data.NewProtectedData
import ru.kochkaev.zixamc.api.sql.util.ChatDataSQLMap
import ru.kochkaev.zixamc.api.telegram.BotLogic
import ru.kochkaev.zixamc.api.telegram.model.TgMessage
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup

abstract class SQLChat(
    val id: Long,
) {
    companion object {
        fun get(id: Long) =
            if (id<0) SQLGroup.get(id)
            else SQLUser.get(id)
    }
    abstract val data: ChatDataSQLMap
    open fun setProtectedInfoMessage(
        message: TgMessage,
        protectLevel: AccountType,
        protectedType: NewProtectedData.ProtectedType,
        senderBotId: Long,
    ) {
        val protected = data.getCasted(ChatDataTypes.PROTECTED) ?: hashMapOf()
        val new = NewProtectedData(
            messageId = message.messageId,
            protectedType = protectedType,
            senderBotId = senderBotId,
        )
        protected[protectLevel]?.add(new) ?: also { protected[protectLevel] = arrayListOf(new) }
        data.set(ChatDataTypes.PROTECTED, protected)
    }
    open suspend fun deleteProtected(protectLevel: AccountType) {
        val protected = data.getCasted(ChatDataTypes.PROTECTED) ?: return
        var level: AccountType? = protectLevel
        while (level!=null){
            protected[level]?.sortedByDescending { it.messageId } ?.forEach { data ->
                data.senderBotId.let { id ->
                    BotLogic.bots.firstOrNull { it.me.id == id }
                } ?.also { bot ->
                    try { when (data.protectedType) {
                        NewProtectedData.ProtectedType.TEXT ->
                            bot.deleteMessage(
                                chatId = id,
                                messageId = data.messageId,
                            )
                        NewProtectedData.ProtectedType.REPLY_MARKUP ->
                            bot.editMessageReplyMarkup(
                                chatId = id,
                                messageId = data.messageId,
                                replyMarkup = TgReplyMarkup()
                            )
                    } } catch (_: Exception) {}
                }
            }
            protected.remove(level)
            level = level.levelHigh
        }
        data.set(ChatDataTypes.PROTECTED, protected)
    }
    abstract suspend fun hasProtectedLevel(level: AccountType): Boolean
    abstract suspend fun sendRulesUpdated(capital: Boolean)
    open suspend fun sendRulesUpdated() = sendRulesUpdated(false)
}