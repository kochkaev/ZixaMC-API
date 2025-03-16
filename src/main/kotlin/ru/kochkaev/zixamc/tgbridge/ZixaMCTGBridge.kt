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
    }
    override fun onInitialize() {
        ConfigManager.init(false)
        MySQLIntegration.startServer()
        RequestsBot.startBot()
        ServerBot.startBot()
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            ZixaMCCommand.registerCommand(dispatcher)
            ReplyCommand.registerCommand(dispatcher)
        }
    }
    fun onServerStopped(server: MinecraftServer) {
        ServerBot.stopBot()
        RequestsBot.stopBot()
        MySQLIntegration.stopServer()
    }
}
