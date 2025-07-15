package ru.kochkaev.zixamc.api.sql

import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataTypes
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.data.NewProtectedData
import ru.kochkaev.zixamc.api.sql.util.ChatDataSQLMap
import ru.kochkaev.zixamc.api.telegram.BotLogic
import ru.kochkaev.zixamc.api.telegram.TelegramBotZixa
import ru.kochkaev.zixamc.api.telegram.model.TgChatMemberAdministrator
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
        val toDelete = arrayListOf<Pair<Int, TelegramBotZixa>>()
        while (level!=null) {
            protected[level]?.sortedByDescending { it.messageId } ?.forEach { data ->
                BotLogic.getBot(data.senderBotId)?.also { bot ->
                    try { when (data.protectedType) {
                        NewProtectedData.ProtectedType.TEXT ->
                            toDelete.add(data.messageId to bot)
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
        val deleter: suspend (TelegramBotZixa, List<Int>) -> Unit = { bot, list ->
            var i = 0
            var fromIndex = 0
            var toIndex: Int
            while (fromIndex<=list.lastIndex) {
                toIndex = minOf(100 * (i + 1), list.size)
                bot.deleteMessages(
                    chatId = id,
                    messageIds = list.subList(fromIndex, toIndex)
                )
                i++
                fromIndex = 100 * i
            }
        }
        (if (this is SQLGroup) {
            BotLogic.bots.firstOrNull { (it.getChatMember(id, it.me.id) as? TgChatMemberAdministrator)?.canDeleteMessages == true }
        } else null)
            ?.let { bot -> deleter(bot, toDelete.map { it.first }) }
            ?: toDelete.fold(hashMapOf<TelegramBotZixa, ArrayList<Int>>()) { aac, (id, bot) ->
                aac.getOrPut(bot) { arrayListOf() }.add(id)
                aac
            }.forEach { (bot, list) -> deleter(bot, list) }
        data.set(ChatDataTypes.PROTECTED, protected)
    }
    abstract suspend fun hasProtectedLevel(level: AccountType): Boolean
    abstract suspend fun sendRulesUpdated(capital: Boolean)
    open suspend fun sendRulesUpdated() = sendRulesUpdated(false)
}