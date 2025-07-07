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
import ru.kochkaev.zixamc.api.command.ZixaMCCommand
import ru.kochkaev.zixamc.api.config.Config
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.api.telegram.ServerBot
import ru.kochkaev.zixamc.audioplayerintegration.ZixaMCAudioPlayerIntegration
import ru.kochkaev.zixamc.chatsync.ChatSync
import ru.kochkaev.zixamc.consoleintegration.ZixaMCConsoleIntegration
import ru.kochkaev.zixamc.easyauthintegration.ZixaMCEasyAuthIntegration
import ru.kochkaev.zixamc.fabrictailorintegration.ZixaMCFabricTailorIntegration
import ru.kochkaev.zixamc.requests.ZixaMCRequests

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
    }
    override fun onInitialize() {
        Initializer.startServerBot()
        ServerBot.bot.registerCallbackQueryHandler("cancel", CancelCallbackData::class.java, CancelCallbackData.ON_SERVER_CALLBACK)
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            ZixaMCCommand.registerCommand(dispatcher)
        }

        ZixaMCAudioPlayerIntegration().onInitialize()
        ChatSync().onInitialize()
        ZixaMCConsoleIntegration().onInitialize()
        ZixaMCEasyAuthIntegration().onInitialize()
        ZixaMCFabricTailorIntegration().onInitialize()
        ZixaMCRequests().onInitialize()
    }
    fun onServerStopped(server: MinecraftServer) {
        Initializer.stop()
    }
}
