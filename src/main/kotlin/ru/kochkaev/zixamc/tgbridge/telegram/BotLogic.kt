package ru.kochkaev.zixamc.tgbridge.telegram

import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotLogic
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgMessage
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager.CONFIG
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.sql.data.AccountType
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.sql.SQLChat
import ru.kochkaev.zixamc.tgbridge.sql.data.NewProtectedData
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyMarkup
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyParameters

object BotLogic {

    val config
        get() = CONFIG!!
    val copyIPReplyMarkup
        get() = TgInlineKeyboardMarkup.TgInlineKeyboardButton(
            text = config.general.lang.buttonCopyServerIP,
            copy_text = TgInlineKeyboardMarkup.TgInlineKeyboardButton.TgCopyTextButton(config.general.serverIP),
        )

    suspend fun sendInfoMessage(
        bot: TelegramBotZixa,
        chat: SQLChat,
        replyParameters: TgReplyParameters? = null,
        replyMarkup: TgReplyMarkup? = null,
    ) : TgMessage {
        val newMessage = bot.sendMessage(
            chatId = chat.id,
            text = escapePlaceholders(config.general.lang.infoMessage),
            replyParameters = replyParameters,
            replyMarkup = replyMarkup,
            protectContent = true,
        )
        chat.setProtectedInfoMessage(
            message = newMessage,
            protectLevel = AccountType.PLAYER,
            protectedType = NewProtectedData.ProtectedType.TEXT,
            senderBotId = bot.me.id,
        )
        return newMessage
    }

    fun escapePlaceholders(text: String, nickname: String? = null, group: SQLGroup = ChatSyncBotLogic.DEFAULT_GROUP) : String {
        return TextParser.formatLang(text,
            "nickname" to (nickname?:""),
            "mentionAll" to group.mentionAll(),
            "serverIP" to config.general.serverIP,
        )
    }
}