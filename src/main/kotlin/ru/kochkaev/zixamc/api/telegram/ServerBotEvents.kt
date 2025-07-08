package ru.kochkaev.zixamc.api.telegram

import net.fabricmc.fabric.api.event.EventFactory

object ServerBotEvents {
    val SERVER_BOT_STARTED = EventFactory.createArrayBacked(ServerBotStarted::class.java) { handlers ->
        ServerBotStarted { bot ->
            for (handler in handlers) {
                handler.onStarted(bot)
            }
        }
    }
    fun interface ServerBotStarted {
        fun onStarted(bot: TelegramBotZixa)
    }
}