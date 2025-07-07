package ru.kochkaev.zixamc.api.telegram

import ru.kochkaev.zixamc.api.config.Config
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.api.telegram.model.TgMessage
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLChat
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.telegram.RulesManager.RulesCallbackData
import ru.kochkaev.zixamc.api.telegram.RulesManager.RulesOperation
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
            RulesManager.updateRules(executor, false)
        }
        bot.registerCommandHandler("rulesUpdatedWithRevoke") {
            val executor = SQLUser.get(it.from?.id?:return@registerCommandHandler)?:return@registerCommandHandler
            RulesManager.updateRules(executor, true)
        }
        bot.registerCommandHandler("selectTopic", ServerBotGroup::selectTopicCommand)
        bot.registerCommandHandler("settings", ServerBotGroup::settingsCommand)

        bot.registerCallbackQueryHandler("menu", Menu.MenuCallbackData::class.java, Menu::onCallback)
        bot.registerCallbackQueryHandler("group", ServerBotGroup.GroupCallback::class.java, ServerBotGroup::onCallback)
        bot.registerCallbackQueryHandler("rules", RulesManager.RulesCallbackData::class.java, RulesManager::onCallback)

        Menu.addIntegration(Menu.Integration.of(
            callbackName = "info",
            menuDisplay = config.menu.infoButton,
            processor = { cbq, sql -> SQLUser.get(cbq.from.id)?.let{
                if (it.hasProtectedLevel(AccountType.PLAYER)) {
                    sendInfoMessage(it)
                    TgCBHandlerResult.DELETE_MESSAGE
                } else null
            } ?: TgCBHandlerResult.SUCCESS },
            filter = { chatId, userId -> chatId == userId },
        ))
        Menu.addIntegration(Menu.Integration.of(
            menuButton = TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                text = config.menu.addToGroupButton,
                url = "https://t.me/${bot.me.username}?startgroup"
            ),
            filter = { chatId, userId -> chatId == userId },
        ))
        Menu.addIntegration(Menu.Integration.of(
            callbackName = "removeAgreedWithRules",
            menuDisplay = config.menu.removeAgreedWithRules,
            processor = { cbq, sql ->
                bot.editMessageText(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    text = Config.config.general.rules.confirmRemoveAgree4player.formatLang("nickname" to (SQLUser.get(cbq.from.id)?.nickname?:""))
                )
                bot.editMessageReplyMarkup(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(listOf(
                        listOf(SQLCallback.of(
                            display = ConfigManager.config.general.buttons.confirm,
                            type = "rules",
                            data = RulesCallbackData(RulesOperation.CONFIRM_REMOVE_AGREE, RulesManager.RulesOperationType.REMOVE_AGREE)
                        )),
                        listOf(SQLCallback.of(
                            display = ConfigManager.config.general.buttons.cancel,
                            type = "rules",
                            data = RulesCallbackData(RulesOperation.CANCEL_REMOVE_AGREE, RulesManager.RulesOperationType.REMOVE_AGREE, cbq.from.id)
                        )),
                    ))
                )
                TgCBHandlerResult.DELETE_LINKED
            },
            filter = { chatId, userId -> chatId == userId && SQLUser.get(userId)?.agreedWithRules == true },
        ))

        bot.registerChatJoinRequestHandler(ServerBotUpdateManager::onTelegramChatJoinRequest)
        bot.registerNewChatMembersHandler(ServerBotGroup::newChatMembers)
        bot.registerLeftChatMemberHandler(ServerBotGroup::leftChatMember)

//        bot.registerMessageHandler(ServerBotGroup::onMessage)
//        bot.registerMessageHandler(Menu::onMessage)
    }
}