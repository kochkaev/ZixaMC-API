package ru.kochkaev.zixamc.api

import kotlinx.coroutines.*
import ru.kochkaev.zixamc.api.sql.MySQL
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLProcess
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.telegram.BotLogic
import ru.kochkaev.zixamc.api.telegram.ServerBot

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
    private val beforeSQLStop = arrayListOf<suspend () -> Unit>()
    fun registerBeforeSQLStopEvent(event: suspend () -> Unit) {
        beforeSQLStop.add(event)
    }

    fun startServerBot() {
        ServerBot.startBot()
        BotLogic.registerBot(ServerBot.bot)
    }
    fun startSQL() {
        MySQL.connect()
        sqlLoaded = true
        registeredSQL.forEach { it.connectTable() }
    }
    fun stop() = coroutineScope.launch {
        beforeSQLStop.forEach { it() }
        ServerBot.stopBot()
        ServerBot.bot.pollTask?.join()
        ServerBot.bot.postTask?.join()
        sqlLoaded = false
        MySQL.close()
        coroutineScope.cancel()
    }
}