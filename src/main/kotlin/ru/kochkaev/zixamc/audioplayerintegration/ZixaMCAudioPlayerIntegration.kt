package ru.kochkaev.zixamc.audioplayerintegration

import net.fabricmc.api.ModInitializer
import ru.kochkaev.zixamc.api.telegram.Menu
import ru.kochkaev.zixamc.api.telegram.ServerBot

class ZixaMCAudioPlayerIntegration: ModInitializer {

    override fun onInitialize() {
        Menu.addIntegration(Menu.Integration.of(
            callbackName = "audioPlayer",
            menuDisplay = ServerBot.config.integration.audioPlayer.buttonMenu,
            processor = AudioPlayerIntegration::callbackProcessor,
            filter = { chatId, userId -> chatId == userId },
        ))
    }

}