package ru.kochkaev.zixamc.tgbridge.serverBot

import ru.kochkaev.zixamc.tgbridge.BotLogic
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.ProtectedMessageData
import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters
import ru.kochkaev.zixamc.tgbridge.easyAuth.EasyAuthIntegration
import ru.kochkaev.zixamc.tgbridge.serverBot.integration.Menu

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
        bot.registerCallbackQueryHandler(ServerBotUpdateManager::onTelegramCallbackQuery)
        bot.registerCommandHandler("start") { Menu.sendMenu(it.chat.id) }
        bot.registerMessageHandler(Menu::onMessage)

        bot.registerCallbackQueryHandler(/*"easyauth", EasyAuthIntegration.EasyAuthCallbackData::class.java,*/ EasyAuthIntegration::onTelegramCallbackQuery)
        bot.registerCallbackQueryHandler(/*"menu", Menu.MenuCallbackData::class.java,*/ Menu::onCallback)
    }

}