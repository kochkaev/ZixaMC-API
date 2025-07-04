package ru.kochkaev.zixamc.tgbridge.telegram.serverBot

import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.api.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup

object ServerBotUpdateManager {

    suspend fun onTelegramCallbackQuery(cbq: TgCallbackQuery) {
        if (cbq.message.chat.id > 0) try {
            bot.editMessageReplyMarkup(
                chatId = cbq.message.chat.id,
                messageId = cbq.message.messageId,
                replyMarkup = TgReplyMarkup()
            )
        } catch (_: Exception) {}
    }
}