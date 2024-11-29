package ru.kochkaev.zixamc.requests

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.context.CommandContextBuilder
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.CommandSource
import net.minecraft.server.MinecraftServer
import net.minecraft.server.WhitelistEntry
import net.minecraft.server.command.ServerCommandSource
import org.slf4j.LoggerFactory
import ru.kochkaev.zixamc.requests.dataclassTelegram.TgMessage

/**
 * @author kochkaev
 */
class ZixaMCRequests : ModInitializer {
    companion object {
        val logger = LoggerFactory.getLogger("ZixaMCRequests")
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
           CommandDispatcher<CommandSource>().execute("easywhitelist add $nickname", server.commandSource);
        }
        fun removeFromWhitelist(nickname: String) {
            val server = FabricLoader.getInstance().gameInstance as MinecraftServer
           CommandDispatcher<CommandSource>().execute("easywhitelist remove $nickname", server.commandSource);
        }
    }
    override fun onInitialize() {
        ConfigManager.init(false)
        RequestsBot.startBot()
        MySQLIntegration.startServer()
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped)
    }
    fun onServerStopped(server: MinecraftServer) {
        RequestsBot.stopBot()
        MySQLIntegration.stopServer()
    }
}
