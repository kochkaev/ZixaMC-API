package ru.kochkaev.zixamc.api.telegram

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import ru.kochkaev.zixamc.api.ZixaMC.Companion.logger
import ru.kochkaev.zixamc.chatsync.ChatSyncBotCore
import ru.kochkaev.zixamc.chatsync.ChatSyncBotLogic
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.easyauthintegration.EasyAuthIntegration
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
    private val lastMessageLock = Mutex()

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
            if (EasyAuthIntegration.isEnabled) {
//                ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
//                    EasyAuthIntegration.onJoin(handler.player)
//                }
                ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
                    EasyAuthIntegration.onLeave(handler.player)
                }
                EasyAuthIntegration.registerEasyAuthHandlers()
            }
            if (config.chatSync.isEnabled) {
                ChatSyncBotLogic.sendServerStartedMessage()
            }
        }
        isInitialized = true
    }

    fun stopBot() {
        coroutineScope.launch {
            bot.shutdown()
        }
        isInitialized = false
    }

    fun withScopeAndLock(fn: suspend () -> Unit) {
        coroutineScope.launch {
            lastMessageLock.withLock {
                fn()
            }
        }
    }
}