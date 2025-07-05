package ru.kochkaev.zixamc.fabrictailorintegration

import net.fabricmc.api.ModInitializer
import ru.kochkaev.zixamc.api.telegram.Menu
import ru.kochkaev.zixamc.api.telegram.ServerBot

class ZixaMCFabricTailorIntegration: ModInitializer {

    override fun onInitialize() {
        Menu.addIntegration(Menu.Integration.of(
            callbackName = "fabricTailor",
            menuDisplay = ServerBot.config.integration.fabricTailor.buttonMenu,
            processor = FabricTailorIntegration::callbackProcessor,
            customDataType = FabricTailorIntegration.AdditionalData::class.java,
            customDataInitial = FabricTailorIntegration.AdditionalData(),
            filter = { chatId, userId -> chatId == userId },
        ))
    }

}