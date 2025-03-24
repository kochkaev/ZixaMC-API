package ru.kochkaev.zixamc.tgbridge.telegram.serverBot

import ru.kochkaev.zixamc.tgbridge.telegram.BotLogic
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotLogic
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgMessage
import ru.kochkaev.zixamc.tgbridge.telegram.easyAuth.EasyAuthIntegration
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.group.ConsoleFeature
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.integration.Menu
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.sql.SQLChat

object ServerBotLogic {

    suspend fun sendInfoMessage(
        chat: SQLChat,
        replyToMessageID: Int? = null,
    ) : TgMessage = BotLogic.sendInfoMessage(
            bot = bot,
            chat = chat,
            replyParameters = if (replyToMessageID!=null) ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyParameters(
                replyToMessageID
            ) else null,
            replyMarkup = ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup(
                listOf(
                    listOf(BotLogic.copyIPReplyMarkup),
                )
            ),
        )

    fun registerTelegramHandlers() {
        ChatSyncBotLogic.registerTelegramHandlers()
        ChatSyncBotLogic.registerMinecraftHandlers()

        bot.registerCallbackQueryHandler(ServerBotUpdateManager::onTelegramCallbackQuery)
        bot.registerCommandHandler("start") { Menu.sendMenu(it.chat.id) }
        bot.registerCommandHandler("mentionAll") { SQLGroup.get(it.chat.id)?.also { group ->
            bot.sendMessage(
                chatId = it.chat.id,
                text = group.mentionAll(),
                replyParameters = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyParameters(
                    it.replyToMessage?.messageId ?: it.messageId
                )
            )
            try { bot.deleteMessage(it.chat.id, it.messageId) } catch (_: Exception) {}
        } }
        bot.registerMessageHandler(Menu::onMessage)

        bot.registerCallbackQueryHandler(/*"easyauth", EasyAuthIntegration.EasyAuthCallbackData::class.java,*/ EasyAuthIntegration::onTelegramCallbackQuery)
        bot.registerCallbackQueryHandler("menu", Menu.MenuCallbackData::class.java, Menu::onCallback)

//        bot.registerBotChatMemberUpdatedHandler(ServerBotGroup::addedToGroup)
        bot.registerNewChatMembersHandler(ServerBotGroup::newChatMembers)
        bot.registerLeftChatMemberHandler(ServerBotGroup::leftChatMember)
        bot.registerCallbackQueryHandler("group", ServerBotGroup.GroupCallback::class.java, ServerBotGroup::onCallback)
        bot.registerMessageHandler(ServerBotGroup::onMessage)
        bot.registerCommandHandler("selectTopic", ServerBotGroup::selectTopicCommand)
        bot.registerCommandHandler("settings", ServerBotGroup::settingsCommand)

        bot.registerMessageHandler(ConsoleFeature::executeCommand)
    }

}