package ru.kochkaev.zixamc.api.telegram

import okhttp3.internal.toImmutableList
import ru.kochkaev.zixamc.chatsync.ChatSyncBotLogic
import ru.kochkaev.zixamc.api.telegram.model.TgMessage
import ru.kochkaev.zixamc.api.config.ConfigManager.config
import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLChat
import ru.kochkaev.zixamc.api.sql.data.NewProtectedData
import ru.kochkaev.zixamc.api.telegram.model.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters

object BotLogic {

    val bots: List<TelegramBotZixa> = arrayListOf()
    fun registerBot(bot: TelegramBotZixa) {
        (bots as ArrayList<TelegramBotZixa>).add(bot)
    }

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
        return text.formatLang(
            "nickname" to (nickname?:""),
            "mentionAll" to group.mentionAll(),
            "serverIP" to config.general.serverIP,
        )
    }
}