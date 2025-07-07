package ru.kochkaev.zixamc.api.telegram

import kotlinx.coroutines.*
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import ru.kochkaev.zixamc.api.ZixaMC.Companion.logger
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.Initializer.coroutineScope

/**
 * @author kochkaev
 */
object ServerBot {

    val server: MinecraftServer
        get() = FabricLoader.getInstance().gameInstance as MinecraftServer
    lateinit var bot: TelegramBotZixa
    val config
        get() = ConfigManager.config.serverBot
    var isInitialized = false

    fun startBot() {
        bot = TelegramBotZixa(config.botAPIURL, config.botToken, logger, config.pollTimeout)
        runBlocking {
            bot.init()
        }
//        bot.registerMessageHandler(this::onTelegramMessage)
//        bot.registerCallbackQueryHandler(this::onTelegramCallbackQuery)
//        bot.registerChatJoinRequestHandlers(this::onTelegramChatJoinRequest)
//        bot.registerCommandHandler("accept", this::onTelegramAcceptCommand)
//        bot.registerCommandHandler("reject", this::onTelegramRejectCommand)
//        bot.registerCommandHandler("promote", this::onTelegramPromoteCommand)
//        bot.registerCommandHandler("kick", this::onTelegramKickCommand)
//        bot.registerCommandHandler("start", this::onTelegramStartCommand)
//        bot.registerCommandHandler("new", this::onTelegramNewCommand)
//        bot.registerCommandHandler("cancel", this::onTelegramCancelCommand)
        coroutineScope.launch {
            bot.startPosting(coroutineScope)
            bot.startPolling(coroutineScope)
//            ZixaMCTGBridge.isServerBotLoaded = true
            ServerBotLogic.registerTelegramHandlers()
        }
        isInitialized = true
    }

    fun stopBot() {
        coroutineScope.launch {
            bot.shutdown()
        }
        isInitialized = false
    }
}