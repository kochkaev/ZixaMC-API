package ru.kochkaev.zixamc.tgbridge.telegram.serverBot

import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgCallbackQuery

object ServerBotUpdateManager {

    suspend fun onTelegramCallbackQuery(cbq: TgCallbackQuery) {
        if (cbq.message.chat.id > 0) try {
            bot.editMessageReplyMarkup(
                chatId = cbq.message.chat.id,
                messageId = cbq.message.messageId,
                replyMarkup = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyMarkup()
            )
        } catch (_: Exception) {}
    }
}