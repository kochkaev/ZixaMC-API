package ru.kochkaev.zixamc.tgbridge

import ru.kochkaev.zixamc.tgbridge.dataclassSQL.ProtectedMessageData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic
import ru.kochkaev.zixamc.tgbridge.serverBot.ServerBotLogic
import ru.kochkaev.zixamc.tgbridge.ConfigManager.CONFIG
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.AccountType
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgInlineKeyboardMarkup

object BotLogic {

    val config
        get() = CONFIG!!
    val copyIPReplyMarkup
        get() = TgInlineKeyboardMarkup.TgInlineKeyboardButton(
            text = config.general.lang.buttonCopyServerIP,
            copy_text = TgInlineKeyboardMarkup.TgInlineKeyboardButton.TgCopyTextButton(config.general.serverIP),
        )

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

    suspend fun deleteAllProtected(protected: List<ProtectedMessageData>, protectLevel: AccountType) {
        deleteAllProtected(protected, protectLevel.getId())
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
        entity: SQLEntity? = null
    ) : TgMessage? {
        val newMessage = bot.sendMessage(
            chatId = chatId,
            text = config.general.lang.infoMessage,
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
        MySQLIntegration.linkedEntities.filter { it.value.accountType.isPlayer() } .forEach {
            output.append("<a href=\"tg://user?id=${it.key}\">$placeholder</a>")
        }
        return output.toString()
    }

    fun escapePlaceholders(text: String, nickname: String? = null) : String {
        return TextParser.formatLang(text,
            "nickname" to (nickname?:""),
            "mentionAll" to getMentionOfAllPlayers(),
            "serverIP" to config.general.serverIP,
        )
    }
}