package ru.kochkaev.zixamc.tgbridge

import kotlinx.coroutines.*
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge.Companion.logger
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotLogic

/**
 * @author kochkaev
 */
object ServerBot {

    val server: MinecraftServer
        get() = FabricLoader.getInstance().gameInstance as MinecraftServer
    lateinit var bot: TelegramBotZixa
    private lateinit var config: Config.ServerBotDataClass
    val coroutineScope = CoroutineScope(Dispatchers.IO).plus(SupervisorJob())

    fun startBot() {
        config = ConfigManager.CONFIG!!.serverBot
        if (!config.isEnabled) return
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
            bot.startPolling(coroutineScope)
            if (config.chatSync.isEnabled) {
                ChatSyncBotCore.init()
                ChatSyncBotLogic.sendServerStartedMessage()
            }
        }
    }
    fun stopBot() {
        if (config.isEnabled) {
            coroutineScope.launch {
                if (config.chatSync.isEnabled) ChatSyncBotLogic.sendServerStoppedMessage()
                bot.shutdown()
            }
            coroutineScope.cancel()
        }
    }
}