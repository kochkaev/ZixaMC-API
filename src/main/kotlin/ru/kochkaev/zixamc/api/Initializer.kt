package ru.kochkaev.zixamc.api

import kotlinx.coroutines.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import ru.kochkaev.zixamc.api.sql.MySQL
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLProcess
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot
import ru.kochkaev.zixamc.api.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.group.ConsoleFeature
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.group.ConsoleLogAppender

object Initializer {
    val job = SupervisorJob()
    val coroutineScope = CoroutineScope(Dispatchers.IO).plus(job)

    private val registeredSQL = arrayListOf(
        SQLCallback, SQLProcess, SQLUser, SQLGroup
    )
    var sqlLoaded = false
        private set
    fun registerSQLTable(sql: MySQL) {
        if (!sqlLoaded) registeredSQL.add(sql)
        else sql.connectTable()
    }

    fun startServerBot() {
        ServerBot.startBot()
    }
    fun startRequestsBot() {
        RequestsBot.startBot()
    }
    fun startSQL() {
        MySQL.connect()
        sqlLoaded = true
        registeredSQL.forEach { it.connectTable() }
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
        sqlLoaded = false
        MySQL.close()
        coroutineScope.cancel()
    }
}