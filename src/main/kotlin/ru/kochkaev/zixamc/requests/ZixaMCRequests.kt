package ru.kochkaev.zixamc.requests

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory
import ru.kochkaev.zixamc.requests.dataclassTelegram.TgMessage

/**
 * @author kochkaev
 */
class ZixaMCRequests : ModInitializer {
    companion object {
        val logger = LoggerFactory.getLogger("ZixaMCRequests")
        fun getFirstReply(msg: TgMessage): TgMessage {
            val repliedMessage = msg.replyToMessage ?: return msg
            return if (repliedMessage.replyToMessage == null) repliedMessage else getFirstReply(repliedMessage)
        }
    }
    override fun onInitialize() {
        ConfigManager.init(false)
        RequestsBot.startBot()
        MySQLIntegration.startServer()
    }
}
