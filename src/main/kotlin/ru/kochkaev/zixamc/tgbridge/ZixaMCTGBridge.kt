package ru.kochkaev.zixamc.tgbridge

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.CommandSource
import net.minecraft.server.MinecraftServer
import org.slf4j.LoggerFactory
import ru.kochkaev.zixamc.tgbridge.command.ZixaMCCommand
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.legecySQL.MySQLIntegration

/**
 * @author kochkaev
 */
class ZixaMCTGBridge : ModInitializer {
    companion object {
        val logger = LoggerFactory.getLogger("ZixaMCTGBridge")
        fun getFirstReply(msg: TgMessage): TgMessage {
//            val repliedMessage = msg.replyToMessage ?: return msg
//            return if (repliedMessage.replyToMessage == null) repliedMessage else getFirstReply(repliedMessage)
            var tempMsg = msg
            while (tempMsg.replyToMessage != null) {
                tempMsg = tempMsg.replyToMessage!!
            }
            return tempMsg
        }
        fun addToWhitelist(nickname: String) {
            val server = FabricLoader.getInstance().gameInstance as MinecraftServer
           CommandDispatcher<CommandSource>().execute(ConfigManager.CONFIG!!.requestsBot.addWhitelistCommand.replace("{nickname}", nickname), server.commandSource);
        }
        fun removeFromWhitelist(nickname: String) {
            val server = FabricLoader.getInstance().gameInstance as MinecraftServer
           CommandDispatcher<CommandSource>().execute(ConfigManager.CONFIG!!.requestsBot.removeWhitelistCommand.replace("{nickname}", nickname), server.commandSource);
        }
    }
    override fun onInitialize() {
        ConfigManager.init(false)
        NewMySQLIntegration.startServer()
        RequestsBot.startBot()
        ServerBot.startBot()
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            ZixaMCCommand.registerCommand(dispatcher)
        }
    }
    fun onServerStopped(server: MinecraftServer) {
        RequestsBot.stopBot()
        MySQLIntegration.stopServer()
    }
}
