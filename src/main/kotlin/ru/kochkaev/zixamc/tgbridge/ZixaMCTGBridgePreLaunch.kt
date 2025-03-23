package ru.kochkaev.zixamc.tgbridge

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import org.apache.logging.log4j.Level
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager
import ru.kochkaev.zixamc.tgbridge.sql.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.LoggerContext
import ru.kochkaev.zixamc.tgbridge.serverBot.group.ConsoleFeature
import ru.kochkaev.zixamc.tgbridge.serverBot.group.ConsoleLogAppender

/**
 * @author kochkaev
 */
class ZixaMCTGBridgePreLaunch : PreLaunchEntrypoint {
    override fun onPreLaunch() {
        ConfigManager.init(false)

        MySQL.connect()
        SQLCallback.connectTable()
        SQLProcess.connectTable()
        SQLEntity.connectTable()
        SQLGroup.connectTable()

        val context = LogManager.getContext(false) as LoggerContext
        val rootLogger = context.configuration.rootLogger
        val customAppender = ConsoleLogAppender.createAppender("ZIXA_consoleFeature", null, null)
        customAppender.start()
        rootLogger.addAppender(customAppender, Level.INFO, null)
        context.updateLoggers()
    }
}
