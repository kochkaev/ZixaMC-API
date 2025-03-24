package ru.kochkaev.zixamc.tgbridge

import com.mojang.brigadier.StringReader
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import org.slf4j.LoggerFactory
import ru.kochkaev.zixamc.tgbridge.command.ReplyCommand
import ru.kochkaev.zixamc.tgbridge.command.ZixaMCCommand
import ru.kochkaev.zixamc.tgbridge.config.Config
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager
import ru.kochkaev.zixamc.tgbridge.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.group.ConsoleFeature
import ru.kochkaev.zixamc.tgbridge.sql.*
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot

/**
 * @author kochkaev
 */
class ZixaMCTGBridge : ModInitializer {
    companion object {
        val logger = LoggerFactory.getLogger("ZixaMCTGBridge")
        val server: MinecraftServer?
            get() = FabricLoader.getInstance().gameInstance as MinecraftServer?
        val tmp: Config.TempConfig
            get() = ConfigManager.CONFIG!!.tmp
        fun runConsoleCommand(command: String) {
            val server = server ?: return
            val dispatcher = server.commandManager.dispatcher
            val parseResults = dispatcher.parse(StringReader(command), server.commandSource)
            dispatcher.execute(parseResults)
        }

        var isRequestsBotLoaded = false
        var isServerBotLoaded = false
        fun executeStopSQL() {
            if (!isServerBotLoaded && !isRequestsBotLoaded)
                MySQL.close()
        }
    }
    override fun onInitialize() {
        RequestsBot.startBot()
        ServerBot.startBot()
        RequestsBot.bot.registerCallbackQueryHandler("cancel", CancelCallbackData::class.java, CancelCallbackData.ON_REQUESTS_CALLBACK)
        ServerBot.bot.registerCallbackQueryHandler("cancel", CancelCallbackData::class.java, CancelCallbackData.ON_SERVER_CALLBACK)

        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            ZixaMCCommand.registerCommand(dispatcher)
            ReplyCommand.registerCommand(dispatcher)
        }

        ConsoleFeature.startPeriodicBroadcast()
    }
    fun onServerStopped(server: MinecraftServer) {
        ServerBot.stopBot()
        RequestsBot.stopBot()
        ConsoleFeature.stopBroadcast()
    }
}
