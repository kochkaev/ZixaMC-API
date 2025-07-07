package ru.kochkaev.zixamc.easyauthintegration

import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import ru.kochkaev.zixamc.api.Initializer
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.telegram.ServerBot

class ZixaMCEasyAuthIntegration: ModInitializer {

    override fun onInitialize() {
        ConfigManager.registerConfig(Config)
        ServerBot.bot.registerCallbackQueryHandler(/*"easyauth", EasyAuthIntegration.EasyAuthCallbackData::class.java,*/AuthManager::onTelegramCallbackQuery)
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
                Initializer.coroutineScope.launch {
                    AuthManager.onLeave(handler.player.nameForScoreboard)
                }
            }
            EasyAuthCustomEvents.UPDATE_PLAYER_AUTHENTICATED_EVENT.register { authenticated, player ->
                Initializer.coroutineScope.launch {
                    if (!authenticated) AuthManager.onJoin(player)
//                   else onLeave(player)}
                }
            }
        }
    }

}