package ru.kochkaev.zixamc.requests

import net.fabricmc.api.ModInitializer
import net.minecraft.server.command.CommandManager
import net.minecraft.text.Text
import ru.kochkaev.zixamc.api.Initializer
import ru.kochkaev.zixamc.api.ZixaMC
import ru.kochkaev.zixamc.api.command.ZixaMCCommand
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataTypes
import ru.kochkaev.zixamc.api.telegram.BotLogic

class ZixaMCRequests: ModInitializer {

    override fun onInitialize() {
        ConfigManager.registerConfig(Config)
        ChatDataTypes.registerType(RequestsChatDataType)
        RequestsBot.startBot()
        BotLogic.registerBot(RequestsBot.bot)
        ZixaMCCommand.registerIntegration(CommandManager.literal("silentRestart")
            .executes { context ->
                ZixaMC.tmp.isSilentRestart = true
                ru.kochkaev.zixamc.api.config.Config.update()
                context.source.sendFeedback({ Text.of("Server restart will not be seen in telegram.") }, true)
                context.source.server.stop(false)
                0
            }
        )
        Initializer.registerBeforeSQLStopEvent {
            RequestsBot.stopBot()
            RequestsBot.bot.pollTask?.join()
            RequestsBot.bot.postTask?.join()
        }
    }

}