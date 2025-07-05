package ru.kochkaev.zixamc.api.telegram

import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.chatsync.ChatSyncBotLogic
import ru.kochkaev.zixamc.api.telegram.model.TgMessage
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLChat
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.telegram.ServerBot.config
import ru.kochkaev.zixamc.api.telegram.model.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters
import ru.kochkaev.zixamc.requests.RequestsBotUpdateManager

object ServerBotLogic {

    suspend fun sendInfoMessage(
        chat: SQLChat,
        replyToMessageID: Int? = null,
    ) : TgMessage = BotLogic.sendInfoMessage(
            bot = bot,
            chat = chat,
            replyParameters = if (replyToMessageID!=null) TgReplyParameters(
                replyToMessageID
            ) else null,
            replyMarkup = TgInlineKeyboardMarkup(
                listOf(
                    listOf(BotLogic.copyIPReplyMarkup),
                )
            ),
        )

    fun registerTelegramHandlers() {
        ChatSyncBotLogic.registerTelegramHandlers()
        ChatSyncBotLogic.registerMinecraftHandlers()

        bot.registerCallbackQueryHandler(ServerBotUpdateManager::onTelegramCallbackQuery)
        bot.registerCommandHandler("start") { Menu.sendMenu(it.chat.id, it.from?.id, it.messageThreadId) }
        bot.registerCommandHandler("menu") { Menu.sendMenu(it.chat.id, it.from?.id, it.messageThreadId) }
        bot.registerCommandHandler("mentionAll") { SQLGroup.get(it.chat.id)?.also { group ->
            bot.sendMessage(
                chatId = it.chat.id,
                text = group.mentionAll(),
                replyParameters = TgReplyParameters(
                    it.replyToMessage?.messageId ?: it.messageId
                )
            )
            try { bot.deleteMessage(it.chat.id, it.messageId) } catch (_: Exception) {}
        } }
//        bot.registerMessageHandler(Menu::onMessage)

        bot.registerCallbackQueryHandler("menu", Menu.MenuCallbackData::class.java, Menu::onCallback)

        Menu.addIntegration(Menu.Integration.of(
            callbackName = "info",
            menuDisplay = config.integration.infoButton,
            processor = { cbq, sql -> SQLUser.get(cbq.from.id)?.let{
                if (it.hasProtectedLevel(AccountType.PLAYER))
                    sendInfoMessage(it)
            } },
            filter = { chatId, userId -> chatId == userId },
        ))
        Menu.addIntegration(Menu.Integration.of(
            menuButton = TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                text = config.integration.addToGroupButton,
                url = "https://t.me/${bot.me.username}?startgroup"
            ),
            filter = { chatId, userId -> chatId == userId },
        ))

//        bot.registerBotChatMemberUpdatedHandler(ServerBotGroup::addedToGroup)
        bot.registerChatJoinRequestHandler(ServerBotUpdateManager::onTelegramChatJoinRequest)
        bot.registerNewChatMembersHandler(ServerBotGroup::newChatMembers)
        bot.registerLeftChatMemberHandler(ServerBotGroup::leftChatMember)
        bot.registerCallbackQueryHandler("group", ServerBotGroup.GroupCallback::class.java, ServerBotGroup::onCallback)
//        bot.registerMessageHandler(ServerBotGroup::onMessage)
        bot.registerCommandHandler("selectTopic", ServerBotGroup::selectTopicCommand)
        bot.registerCommandHandler("settings", ServerBotGroup::settingsCommand)
    }

}