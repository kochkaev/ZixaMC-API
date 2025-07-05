package ru.kochkaev.zixamc.easyauthintegration

import net.fabricmc.api.ModInitializer
import ru.kochkaev.zixamc.api.telegram.ServerBot

class ZixaMCEasyAuthIntegration: ModInitializer {

    override fun onInitialize() {
        ServerBot.bot.registerCallbackQueryHandler(/*"easyauth", EasyAuthIntegration.EasyAuthCallbackData::class.java,*/ EasyAuthIntegration::onTelegramCallbackQuery)
    }

}