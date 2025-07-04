package ru.kochkaev.zixamc.api

import com.mojang.brigadier.StringReader
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.kochkaev.zixamc.tgbridge.command.ReplyCommand
import ru.kochkaev.zixamc.api.command.ZixaMCCommand
import ru.kochkaev.zixamc.tgbridge.config.Config
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot
import ru.kochkaev.zixamc.api.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotLogic

/**
 * @author kochkaev
 */
class ZixaMC : ModInitializer {
    companion object {
        val logger: Logger = LoggerFactory.getLogger("ZixaMCTGBridge")
        val server: MinecraftServer?
            get() = FabricLoader.getInstance().gameInstance as MinecraftServer?
        val tmp: Config.TempConfig
            get() = ConfigManager.config.tmp
        fun runConsoleCommand(command: String) {
            val server = server ?: return
            val dispatcher = server.commandManager.dispatcher
            val parseResults = dispatcher.parse(StringReader(command), server.commandSource)
            dispatcher.execute(parseResults)
        }

//        var isRequestsBotLoaded = false
//        var isServerBotLoaded = false
//        fun executeStopSQL() {
//            if (!isServerBotLoaded && !isRequestsBotLoaded)
//                MySQL.close()
//        }
    }
    override fun onInitialize() {
//        RequestsBot.startBot()
//        ServerBot.startBot()
        Initializer.startRequestsBot()
        Initializer.startServerBot()
        RequestsBot.bot.registerCallbackQueryHandler("cancel", CancelCallbackData::class.java, CancelCallbackData.ON_REQUESTS_CALLBACK)
        ServerBot.bot.registerCallbackQueryHandler("cancel", CancelCallbackData::class.java, CancelCallbackData.ON_SERVER_CALLBACK)

        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            ZixaMCCommand.registerCommand(dispatcher)
            ReplyCommand.registerCommand(dispatcher)
        }

//        ConsoleFeature.startPeriodicBroadcast()
        Initializer.startConsoleSync()
    }
    fun onServerStopped(server: MinecraftServer) {
//        ServerBot.stopBot()
//        RequestsBot.stopBot()
        runBlocking {
            if (ServerBot.config.chatSync.isEnabled) ChatSyncBotLogic.sendServerStoppedMessage()
        }
        Initializer.stop()
    }
}
