package ru.kochkaev.zixamc.api.telegram

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
    fun getBot(id: Long) = bots.firstOrNull { it.me.id == id }

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
            text = config.general.lang.infoMessage,
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

    /** Supplier: (chatId) -> String */
    private val globalPlaceholders: ArrayList<Pair<String, (Long) -> String>> = arrayListOf(
        "mentionAll" to { SQLGroup.get(it)?.mentionAll()?:"" },
        "serverIP" to { config.general.serverIP }
    )
    /** Supplier: (chatId) -> String */
    fun registerGlobalPlaceholders(vararg placeholders: Pair<String, (Long) -> String>) {
        globalPlaceholders.addAll(placeholders)
    }
    /** Supplier: (chatId) -> String */
    fun registerGlobalPlaceholders(placeholders: List<Pair<String, (Long) -> String>>) {
        globalPlaceholders.addAll(placeholders)
    }
    fun processGlobalPlaceholders(text: String, chatId: Long): String {
        return text.formatLang(args = globalPlaceholders.map { Pair(it.first, it.second(chatId)) } .toTypedArray())
    }
}