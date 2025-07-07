package ru.kochkaev.zixamc.requests

import kotlinx.coroutines.sync.Mutex
import ru.kochkaev.zixamc.chatsync.LastMessage

interface ChatSyncSQLGroup {
    fun `chatsync$getLastMessage`(): LastMessage?
    fun `chatsync$setLastMessage`(lastMessage: LastMessage?)
    fun `chatsync$getLastMessageLock`(): Mutex
}