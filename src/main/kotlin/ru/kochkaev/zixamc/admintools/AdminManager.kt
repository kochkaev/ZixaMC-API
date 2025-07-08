package ru.kochkaev.zixamc.admintools

import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.telegram.AdminPanel
import ru.kochkaev.zixamc.api.telegram.ServerBot
import ru.kochkaev.zixamc.api.telegram.model.TgCallbackQuery

object AdminManager {
    suspend fun onCallbackUsers(cbq: TgCallbackQuery, sql: SQLCallback<AdminPanel.AdminPanelCallback<UsersManagerCallback>>): TgCBHandlerResult {
        // TODO: Implement users manager logic
        ServerBot.bot.editMessageText(
            chatId = cbq.message.chat.id,
            messageId = cbq.message.messageId,
            text = "Coming soon..."
        )
        ServerBot.bot.editMessageReplyMarkup(
            chatId = cbq.message.chat.id,
            messageId = cbq.message.messageId,
            replyMarkup = TgMenu(listOf(listOf(AdminPanel.BACK_BUTTON)))
        )
        return TgCBHandlerResult.DELETE_LINKED
    }
    class UsersManagerCallback()

    suspend fun onCallbackGroups(cbq: TgCallbackQuery, sql: SQLCallback<AdminPanel.AdminPanelCallback<GroupsManagerCallback>>): TgCBHandlerResult {
        // TODO: Implement groups manager logic
        ServerBot.bot.editMessageText(
            chatId = cbq.message.chat.id,
            messageId = cbq.message.messageId,
            text = "Coming soon..."
        )
        ServerBot.bot.editMessageReplyMarkup(
            chatId = cbq.message.chat.id,
            messageId = cbq.message.messageId,
            replyMarkup = TgMenu(listOf(listOf(AdminPanel.BACK_BUTTON)))
        )
        return TgCBHandlerResult.DELETE_LINKED
    }
    class GroupsManagerCallback()

    suspend fun onCallbackPlayers(cbq: TgCallbackQuery, sql: SQLCallback<AdminPanel.AdminPanelCallback<PlayersManagerCallback>>): TgCBHandlerResult {
        // TODO: Implement players manager logic
        ServerBot.bot.editMessageText(
            chatId = cbq.message.chat.id,
            messageId = cbq.message.messageId,
            text = "Coming soon..."
        )
        ServerBot.bot.editMessageReplyMarkup(
            chatId = cbq.message.chat.id,
            messageId = cbq.message.messageId,
            replyMarkup = TgMenu(listOf(listOf(AdminPanel.BACK_BUTTON)))
        )
        return TgCBHandlerResult.DELETE_LINKED
    }
    class PlayersManagerCallback()
}