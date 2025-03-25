package ru.kochkaev.zixamc.tgbridge.telegram

import kotlinx.coroutines.*
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge.Companion.logger
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager
import ru.kochkaev.zixamc.tgbridge.telegram.requests.RequestsBotCommands
import ru.kochkaev.zixamc.tgbridge.telegram.requests.RequestsBotUpdateManager
import ru.kochkaev.zixamc.tgbridge.Initializer.coroutineScope

/**
 * @author kochkaev
 */
object RequestsBot {
    lateinit var bot: TelegramBotZixa
    val config
        get() = ConfigManager.CONFIG!!.requestsBot
    var isInitialized = false

    fun startBot() {
        if (!config.isEnabled) return
        bot = TelegramBotZixa(config.botAPIURL, config.botToken, logger, config.pollTimeout)
        runBlocking {
            bot.init()
        }
        bot.registerMessageHandler(RequestsBotUpdateManager::onTelegramMessage)
        bot.registerCallbackQueryHandler(RequestsBotUpdateManager::onTelegramCallbackQuery)
        bot.registerChatJoinRequestHandler(RequestsBotUpdateManager::onTelegramChatJoinRequest)
        bot.registerCommandHandler("accept", RequestsBotCommands::onTelegramAcceptCommand)
        bot.registerCommandHandler("reject", RequestsBotCommands::onTelegramRejectCommand)
        bot.registerCommandHandler("promote", RequestsBotCommands::onTelegramPromoteCommand)
        bot.registerCommandHandler("kick", RequestsBotCommands::onTelegramKickCommand)
        bot.registerCommandHandler("restrict", RequestsBotCommands::onTelegramRestrictCommand)
        bot.registerCommandHandler("leave", RequestsBotCommands::onTelegramLeaveCommand)
        bot.registerCommandHandler("return", RequestsBotCommands::onTelegramReturnCommand)
        bot.registerCommandHandler("rulesUpdated", RequestsBotCommands::onTelegramRulesUpdatedCommand)
        bot.registerCommandHandler("rulesUpdatedWithRevoke", RequestsBotCommands::onTelegramRulesUpdatedWithRevokeCommand)
        bot.registerCommandHandler("start", RequestsBotCommands::onTelegramStartCommand)
        bot.registerCommandHandler("new", RequestsBotCommands::onTelegramNewCommand)
        bot.registerCommandHandler("cancel", RequestsBotCommands::onTelegramCancelCommand)
        coroutineScope.launch {
            bot.startPolling(coroutineScope)
//            ZixaMCTGBridge.isRequestsBotLoaded = true
        }
        isInitialized = true
    }
    fun stopBot() {
        if (config.isEnabled) {
            coroutineScope.launch {
                bot.shutdown()
                ZixaMCTGBridge.logger.info("RequestsBot job canceled")
//                ZixaMCTGBridge.isRequestsBotLoaded = false
//                ZixaMCTGBridge.executeStopSQL()
//                job.cancelAndJoin()
            }
        }
        isInitialized = false
    }
}