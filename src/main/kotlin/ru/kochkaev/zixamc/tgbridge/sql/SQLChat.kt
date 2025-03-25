package ru.kochkaev.zixamc.tgbridge.sql

import ru.kochkaev.zixamc.tgbridge.telegram.model.TgMessage
import ru.kochkaev.zixamc.tgbridge.sql.data.AccountType
import ru.kochkaev.zixamc.tgbridge.sql.data.ChatData
import ru.kochkaev.zixamc.tgbridge.sql.data.NewProtectedData
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyMarkup

abstract class SQLChat(
    val id: Long,
) {
    companion object {
        fun get(id: Long) =
            if (id<0) SQLGroup.get(id)
            else SQLEntity.get(id)
    }
    abstract val dataGetter: () -> ChatData
    abstract val dataSetter: (ChatData) -> Unit
    open fun setProtectedInfoMessage(
        message: TgMessage,
        protectLevel: AccountType,
        protectedType: NewProtectedData.ProtectedType,
        senderBotId: Long,
    ) {
        dataSetter(dataGetter().apply {
            val data = NewProtectedData(
                messageId = message.messageId,
                protectedType = protectedType,
                senderBotId = senderBotId,
            )
            this.protected[protectLevel]?.add(data) ?: also { this.protected[protectLevel] = arrayListOf(data) }
        })
    }
    open suspend fun deleteProtected(protectLevel: AccountType) {
        dataSetter(dataGetter().apply {
            var level: AccountType? = protectLevel
            while (level!=null){
                this.protected[level]?.sortedByDescending { it.messageId } ?.forEach { data ->
                    data.senderBotId.let { id ->
                        when (id) {
                            ServerBot.bot.me.id -> ServerBot.bot
                            RequestsBot.bot.me.id -> RequestsBot.bot
                            else -> null
                        }
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
                this.protected.remove(level)
                level = level.levelHigh
            }
        })
    }
    abstract suspend fun hasProtectedLevel(level: AccountType): Boolean
}