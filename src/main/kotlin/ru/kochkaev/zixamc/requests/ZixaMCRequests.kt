package ru.kochkaev.zixamc.requests

import net.fabricmc.api.ModInitializer
import ru.kochkaev.zixamc.api.Initializer
import ru.kochkaev.zixamc.api.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataTypes

class ZixaMCRequests: ModInitializer {

    override fun onInitialize() {
        ChatDataTypes.registerType(RequestsChatDataType)
        RequestsBot.startBot()
        RequestsBot.bot.registerCallbackQueryHandler("cancel", CancelCallbackData::class.java, CancelCallbackData.ON_REQUESTS_CALLBACK)
        Initializer.registerBeforeSQLStopEvent {
            RequestsBot.stopBot()
            RequestsBot.bot.pollTask?.join()
            RequestsBot.bot.postTask?.join()
        }
    }

}