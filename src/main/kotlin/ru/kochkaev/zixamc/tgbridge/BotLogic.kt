package ru.kochkaev.zixamc.tgbridge

import ru.kochkaev.zixamc.tgbridge.dataclassSQL.ProtectedMessageData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic
import ru.kochkaev.zixamc.tgbridge.serverBot.ServerBotLogic
import ru.kochkaev.zixamc.tgbridge.ConfigManager.CONFIG
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser

object BotLogic {

    suspend fun deleteProtected(
        bot: TelegramBotZixa,
        protected: List<ProtectedMessageData>,
        protectLevel: Int,
    ) {
        protected.filter { it.sender_bot_id == bot.me.id } .filter { it.protect_level < protectLevel } .forEach {
            try {
                when (it.protected_type) {
                    "reply_markup" -> bot.editMessageReplyMarkup(
                        chatId = it.chat_id,
                        messageId = it.message_id.toInt(),
                        replyMarkup = TgReplyMarkup()
                    )

                    "text" -> bot.deleteMessage(
                        chatId = it.chat_id,
                        messageId = it.message_id.toInt(),
                    )
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteAllProtected(
        protected: List<ProtectedMessageData>,
        protectLevel: Int,
    ) {
        if (ServerBot.isInitialized) ServerBotLogic.deleteProtected(protected, protectLevel)
        if (RequestsBot.isInitialized) RequestsLogic.deleteProtected(protected, protectLevel)
    }

    suspend fun sendInfoMessage(
        bot: TelegramBotZixa,
        chatId: Long,
        replyParameters: TgReplyParameters? = null,
        replyMarkup: TgReplyMarkup? = null,
        entity: NewSQLEntity? = null
    ) : TgMessage? {
        val newMessage = bot.sendMessage(
            chatId = chatId,
            text = CONFIG?.requestsBot?.text?.messages?.textInfoMessage?:return null,
            replyParameters = replyParameters,
            replyMarkup = replyMarkup,
            protectContent = true,
        )
        entity?.setProtectedInfoMessage(
            message = newMessage,
            protectLevel = 1,
            protectedType = "text",
            senderBotId = bot.me.id,
        )
        return newMessage
    }

    fun getMentionOfAllPlayers() : String {
        val output = StringBuilder()
        val placeholder = CONFIG?.serverBot?.mentionAllReplaceWith?:"+"
        NewMySQLIntegration.linkedEntities.filter { it.value.accountType <= 1 } .forEach {
            output.append("<a href=\"tg://user?id=${it.key}\">$placeholder</a>")
        }
        return output.toString()
    }

    fun escapePlaceholders(text: String, nickname: String? = null) : String {
        return TextParser.formatLang(text,
            "nickname" to (nickname?:""),
            "mentionAll" to getMentionOfAllPlayers(),
            "serverIP" to (CONFIG?.requestsBot?.serverIP?:""),
        )
    }
}