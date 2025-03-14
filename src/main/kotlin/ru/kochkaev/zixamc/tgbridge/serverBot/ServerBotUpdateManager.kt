package ru.kochkaev.zixamc.tgbridge.serverBot

import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgCallbackQuery
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyMarkup
import ru.kochkaev.zixamc.tgbridge.easyAuth.EasyAuthIntegration
import ru.kochkaev.zixamc.tgbridge.serverBot.integration.Menu

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