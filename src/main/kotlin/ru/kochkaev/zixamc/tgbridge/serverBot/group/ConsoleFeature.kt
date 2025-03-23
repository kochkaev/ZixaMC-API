package ru.kochkaev.zixamc.tgbridge.serverBot.group

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.kochkaev.zixamc.tgbridge.ServerBot
import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.ServerBot.isInitialized
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotLogic
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.FeatureTypes

object ConsoleFeature {
    private val groups: HashMap<Long, Int?> = SQLGroup.groups.fold(hashMapOf()) { acc, linked ->
        linked.getSQL()?.also {
            it.features.getCasted(FeatureTypes.CONSOLE)?.run {
                acc[it.chatId] = this.topicId
            }
        }
        acc
    }
    private val accumulatedMessages = StringBuilder()
    private var lastMessage: String? = null
    private val lastedMessage: HashMap<Long, Int?> = hashMapOf()
    private val broadcastLock = Mutex()
    private val coroutineScope = CoroutineScope(Dispatchers.IO).plus(SupervisorJob())
    private suspend fun broadcast(message: String) {
        groups.forEach { (chatId, topicId) ->
            if (listOf(true, null).contains(lastedMessage[chatId]?.let { lastMessage!=null && lastMessage!!.length + message.length > 4096 })) {
                try {
                    lastedMessage[chatId] = bot.sendMessage(
                        chatId = chatId,
                        text = message,
                        messageThreadId = topicId
                    ).messageId
                    lastMessage = message
                } catch (_: Exception) {}
            } else {
                try {
                    lastedMessage[chatId] = lastedMessage[chatId]!!.let {
                        lastMessage += message
                        bot.editMessageText(
                            chatId = chatId,
                            messageId = it,
                            text = lastMessage!!,
                        ).messageId
                    }
                } catch (_: Exception) {}
            }
        }
    }
    fun addToBroadcast(message: String) {
        accumulatedMessages.append("\n" + message)
    }
    fun startPeriodicBroadcast() {
        coroutineScope.launch {
            broadcast(config.integration.group.features.console.newSession)
            while (isActive) {
                delay(10_000L) // ожидание 10 секунд
                val messagesToSend = broadcastLock.withLock {
                    if (accumulatedMessages.isNotEmpty()) {
                        val content = accumulatedMessages.toString()
                        accumulatedMessages.clear()
                        content
                    } else null
                }
                messagesToSend?.let { broadcast(it) }
            }
        }
    }
    fun stopBroadcast() {
        coroutineScope.launch {
            broadcast(config.integration.group.features.console.stopSession)
            coroutineScope.cancel()
        }
    }
    fun executeCommand(msg: TgMessage) = withScopeAndLock {
        val group = SQLGroup.get(msg.chat.id) ?: return@withScopeAndLock
        val data = group.features.getCasted(FeatureTypes.CONSOLE) ?: return@withScopeAndLock
        if (msg.messageThreadId != data.topicId) return@withScopeAndLock
        val message = msg.effectiveText ?: return@withScopeAndLock
        try {
            ZixaMCTGBridge.runConsoleCommand(message)
            lastedMessage[group.chatId] = null
            lastMessage = null
        } catch (e: Exception) {
            e.message?.also {
                bot.sendMessage(
                    chatId = group.chatId,
                    text = TextParser.escapeHTML(it),
                    messageThreadId = data.topicId,
                    replyParameters = TgReplyParameters(msg.messageId)
                )
            }
        }
    }

    fun withScopeAndLock(fn: suspend () -> Unit) {
        coroutineScope.launch {
            broadcastLock.withLock {
                fn()
            }
        }
    }
}