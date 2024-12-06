package ru.kochkaev.zixamc.tgbridge

import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotLogic

/**
 * @author kochkaev
 */
object ServerBot {
    fun startBot() {
        ChatSyncBotLogic.start()
    }
}