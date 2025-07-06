package ru.kochkaev.zixamc.api.telegram

import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.api.telegram.model.TgMessage
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLChat
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.telegram.ServerBot.config
import ru.kochkaev.zixamc.api.telegram.model.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters

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
        bot.registerCommandHandler("rulesUpdated") {
            val executor = SQLUser.get(it.from?.id?:return@registerCommandHandler)?:return@registerCommandHandler
            updateRules(executor, false)
        }
        bot.registerCommandHandler("rulesUpdatedWithRevoke") {
            val executor = SQLUser.get(it.from?.id?:return@registerCommandHandler)?:return@registerCommandHandler
            updateRules(executor, true)
        }
        bot.registerCommandHandler("selectTopic", ServerBotGroup::selectTopicCommand)
        bot.registerCommandHandler("settings", ServerBotGroup::settingsCommand)

        bot.registerCallbackQueryHandler("menu", Menu.MenuCallbackData::class.java, Menu::onCallback)
        bot.registerCallbackQueryHandler("group", ServerBotGroup.GroupCallback::class.java, ServerBotGroup::onCallback)

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

        bot.registerChatJoinRequestHandler(ServerBotUpdateManager::onTelegramChatJoinRequest)
        bot.registerNewChatMembersHandler(ServerBotGroup::newChatMembers)
        bot.registerLeftChatMemberHandler(ServerBotGroup::leftChatMember)

//        bot.registerMessageHandler(ServerBotGroup::onMessage)
//        bot.registerMessageHandler(Menu::onMessage)
    }

    suspend fun updateRules(
        executor: SQLUser,
        revokeAccepts: Boolean = false,
    ): Boolean {
        if (!executor.hasProtectedLevel(AccountType.ADMIN)) return false
        SQLGroup.groups.forEach { it.sendRulesUpdated(revokeAccepts) }
        SQLUser.users.filter { it.agreedWithRules } .forEach { it.sendRulesUpdated(revokeAccepts) }
        return true
    }
}