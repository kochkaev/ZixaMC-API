package ru.kochkaev.zixamc.api.telegram

import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.feature.FeatureTypes
import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.api.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.api.telegram.model.TgChatJoinRequest
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup
import ru.kochkaev.zixamc.requests.RequestsBot

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
    suspend fun onTelegramChatJoinRequest(request: TgChatJoinRequest) {
        val group = SQLGroup.get(request.chat.id)?:return
        if (group.features.getCasted(FeatureTypes.PLAYERS_GROUP)?.autoAccept == true) {
            val user = SQLUser.get(request.from.id)?:return
            if (user.isRestricted) return
            if (user.accountType.isHigherThanOrEqual(AccountType.PLAYER)) {
                try {
                    RequestsBot.bot.approveChatJoinRequest(request.chat.id, request.from.id)
                } catch (_: Exception) { try {
                    ServerBot.bot.approveChatJoinRequest(request.chat.id, request.from.id)
                } catch (_: Exception) {} }
                group.members.add(request.from.id)
            }
        }
    }
}