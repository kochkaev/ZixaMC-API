package ru.kochkaev.zixamc.tgbridge.serverBot

import ru.kochkaev.zixamc.tgbridge.BotLogic
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.ProtectedMessageData
import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotLogic
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters
import ru.kochkaev.zixamc.tgbridge.easyAuth.EasyAuthIntegration
import ru.kochkaev.zixamc.tgbridge.serverBot.group.ServerBotGroupUpdateManager
import ru.kochkaev.zixamc.tgbridge.serverBot.integration.Menu
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup

object ServerBotLogic {

    suspend fun deleteProtected(
        protected: List<ProtectedMessageData>,
        protectLevel: Int,
    ) = BotLogic.deleteProtected(
        bot = bot,
        protected = protected,
        protectLevel = protectLevel,
    )

    suspend fun sendInfoMessage(
        entity: SQLEntity,
        replyToMessageID: Int? = null,
    ) : TgMessage? = BotLogic.sendInfoMessage(
            bot = bot,
            chatId = entity.userId,
            replyParameters = if (replyToMessageID!=null) TgReplyParameters(replyToMessageID) else null,
            replyMarkup = TgInlineKeyboardMarkup(listOf(
                listOf(BotLogic.copyIPReplyMarkup),
            )),
            entity = entity,
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
                replyParameters = TgReplyParameters(it.replyToMessage?.messageId?:it.messageId)
            )
            try { bot.deleteMessage(it.chat.id, it.messageId) } catch (_: Exception) {}
        } }
        bot.registerMessageHandler(Menu::onMessage)

        bot.registerCallbackQueryHandler(/*"easyauth", EasyAuthIntegration.EasyAuthCallbackData::class.java,*/ EasyAuthIntegration::onTelegramCallbackQuery)
        bot.registerCallbackQueryHandler("menu", Menu.MenuCallbackData::class.java, Menu::onCallback)

        bot.registerBotChatMemberUpdatedHandler(ServerBotGroupUpdateManager::addedToGroup)
        bot.registerCallbackQueryHandler("group", ServerBotGroupUpdateManager.GroupCallback::class.java, ServerBotGroupUpdateManager::onCallback)
        bot.registerMessageHandler(ServerBotGroupUpdateManager::onMessage)
        bot.registerCommandHandler("selectTopic", ServerBotGroupUpdateManager::selectTopicCommand)
        bot.registerCommandHandler("settings", ServerBotGroupUpdateManager::settingsCommand)
    }

}