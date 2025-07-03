package ru.kochkaev.zixamc.tgbridge

import kotlinx.coroutines.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import ru.kochkaev.zixamc.tgbridge.sql.*
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.group.ConsoleFeature
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.group.ConsoleLogAppender

object Initializer {
    val job = SupervisorJob()
    val coroutineScope = CoroutineScope(Dispatchers.IO).plus(job)

    fun startServerBot() {
        ServerBot.startBot()
    }
    fun startRequestsBot() {
        RequestsBot.startBot()
    }
    fun startSQL() {
        MySQL.connect()
        SQLCallback.connectTable()
        SQLProcess.connectTable()
        SQLUser.connectTable()
        SQLGroup.connectTable()
    }
    fun startConsoleSync() {
        val context = LogManager.getContext(false) as LoggerContext
        val rootLogger = context.configuration.rootLogger
        val customAppender = ConsoleLogAppender.createAppender("ZIXA_consoleFeature", null, null)
        customAppender.start()
        rootLogger.addAppender(customAppender, Level.INFO, null)
        context.updateLoggers()
        ConsoleFeature.startPeriodicBroadcast()
    }
    fun stop() = coroutineScope.launch {
        ConsoleFeature.stopBroadcast()
        ConsoleFeature.job.join()
        RequestsBot.stopBot()
        RequestsBot.bot.pollTask?.join()
        RequestsBot.bot.postTask?.join()
        ServerBot.stopBot()
        ServerBot.bot.pollTask?.join()
        ServerBot.bot.postTask?.join()
        MySQL.close()
        coroutineScope.cancel()
    }
}