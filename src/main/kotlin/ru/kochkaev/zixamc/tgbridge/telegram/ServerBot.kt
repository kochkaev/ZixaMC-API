package ru.kochkaev.zixamc.tgbridge.telegram

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge.Companion.logger
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotCore
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotLogic
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager
import ru.kochkaev.zixamc.tgbridge.telegram.easyAuth.EasyAuthIntegration
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.ServerBotLogic
import ru.kochkaev.zixamc.tgbridge.Initializer.coroutineScope
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.integration.AudioPlayerIntegration
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.integration.Menu

/**
 * @author kochkaev
 */
object ServerBot {

    val server: MinecraftServer
        get() = FabricLoader.getInstance().gameInstance as MinecraftServer
    lateinit var bot: TelegramBotZixa
    val config
        get() = ConfigManager.CONFIG!!.serverBot
    var isInitialized = false
    private val lastMessageLock = Mutex()

    fun startBot() {
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
                ChatSyncBotCore.init()
                ChatSyncBotLogic.sendServerStartedMessage()
            }
        }
        isInitialized = true
    }

    fun stopBot() {
        if (config.isEnabled) {
            coroutineScope.launch {
//                if (config.chatSync.isEnabled) ChatSyncBotLogic.sendServerStoppedMessage()
                bot.shutdown()
//                ZixaMCTGBridge.isServerBotLoaded = false
//                ZixaMCTGBridge.executeStopSQL()
//                job.cancelAndJoin()
            }
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