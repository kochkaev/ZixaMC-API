package ru.kochkaev.zixamc.tgbridge

import com.mojang.brigadier.StringReader
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import org.slf4j.LoggerFactory
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.command.ZixaMCCommand

/**
 * @author kochkaev
 */
class ZixaMCTGBridge : ModInitializer {
    companion object {
        val logger = LoggerFactory.getLogger("ZixaMCTGBridge")
        fun addToWhitelist(nickname: String) =
            runConsoleCommand(TextParser.formatLang(ConfigManager.CONFIG!!.requestsBot.addWhitelistCommand, "nickname" to nickname))
        fun removeFromWhitelist(nickname: String) {
            val server = FabricLoader.getInstance().gameInstance as MinecraftServer
            runConsoleCommand(TextParser.formatLang(ConfigManager.CONFIG!!.requestsBot.removeWhitelistCommand, "nickname" to nickname))
        }
        fun runConsoleCommand(command: String) {
            val server = FabricLoader.getInstance().gameInstance as MinecraftServer
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
        }
    }
    fun onServerStopped(server: MinecraftServer) {
        ServerBot.stopBot()
        RequestsBot.stopBot()
        MySQLIntegration.stopServer()
    }
}
