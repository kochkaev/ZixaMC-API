package ru.kochkaev.zixamc.requests

import kotlinx.coroutines.*
import ru.kochkaev.zixamc.requests.ZixaMCRequests.Companion.logger
import ru.kochkaev.zixamc.requests.dataclass.TgInputPollOption
import ru.kochkaev.zixamc.requests.dataclass.TgMessage
import ru.kochkaev.zixamc.requests.dataclass.TgReplyParameters

/**
 * @author kochkaev
 */
object RequestsBot {
    lateinit var bot: TelegramBotZixa
    private val coroutineScope = CoroutineScope(Dispatchers.IO).plus(SupervisorJob())

    fun startBot() {
        bot = TelegramBotZixa(ConfigManager.CONFIG?.botAPIURL!!, ConfigManager.CONFIG?.botToken!!, logger)
        runBlocking {
            bot.init()
        }
        bot.registerMessageHandler(this::onTelegramMessage)
        bot.registerCommandHandler("accept", this::onTelegramAcceptCommand)
        bot.registerCommandHandler("reject", this::onTelegramRejectCommand)
        coroutineScope.launch {
            bot.startPolling(coroutineScope)
        }
    }

    suspend fun onTelegramMessage(msg: TgMessage) {
        if (ConfigManager.CONFIG == null) return
        val firstReply = ZixaMCRequests.getFirstReply(msg)
        if (
            (
                    msg.chat.id == ConfigManager.CONFIG!!.targetChatId
                            || ConfigManager.CONFIG!!.targetTopicId!=0 && msg.messageThreadId == ConfigManager.CONFIG!!.targetTopicId
                    ) && ConfigManager.CONFIG!!.forwardBack
            && (msg.replyToMessage != null && firstReply.from == bot.me && firstReply.forwardFrom != null)
        ) {
            bot.forwardMessage(
                firstReply.forwardFrom.id,
                fromChatId = msg.chat.id,
                messageId = msg.messageId
            )
        }
        else {
            val forwardedMessage = bot.forwardMessage(
                chatId = ConfigManager.CONFIG!!.targetChatId,
                messageThreadId = ConfigManager.CONFIG!!.targetTopicId,
                fromChatId = msg.chat.id,
                messageId = msg.messageId
            )
            if (ConfigManager.CONFIG!!.textOnSend4Target.isNotEmpty()) bot.sendMessage(
                chatId = ConfigManager.CONFIG!!.targetChatId,
                text = ConfigManager.CONFIG!!.textOnSend4Target,
                replyToMessageId = forwardedMessage.messageId,
//                ConfigManager.CONFIG!!.targetTopicId,
            )
            if (ConfigManager.CONFIG!!.autoCreatePoll) bot.sendPoll(
                chatId = ConfigManager.CONFIG!!.targetChatId,
                messageThreadId = ConfigManager.CONFIG!!.targetTopicId,
                question = ConfigManager.CONFIG!!.pollQuestion.replace("{username}", "@${msg.senderUserName}"),
                options = listOf(
                    TgInputPollOption(ConfigManager.CONFIG!!.pollAnswerTrue),
                    TgInputPollOption(ConfigManager.CONFIG!!.pollAnswerNull),
                    TgInputPollOption(ConfigManager.CONFIG!!.pollAnswerFalse),
                ),
                replyParameters = TgReplyParameters(
                    message_id = forwardedMessage.messageId,
                ),
            )
        }
    }
    suspend fun onTelegramAcceptCommand(msg: TgMessage): Boolean {
        val firstReply = ZixaMCRequests.getFirstReply(msg)
        if (ConfigManager.CONFIG == null || firstReply.forwardFrom == null) return false
        if (ConfigManager.CONFIG!!.textOnAccept4Target.isNotEmpty()) bot.sendMessage(
            chatId = ConfigManager.CONFIG!!.targetChatId,
            text = ConfigManager.CONFIG!!.textOnAccept4Target,
            replyToMessageId = firstReply.messageId,
        )
        if (ConfigManager.CONFIG!!.textOnAccept4User.isNotEmpty()) bot.sendMessage(
            chatId = firstReply.forwardFrom.id,
            text = ConfigManager.CONFIG!!.textOnAccept4User,
            replyToMessageId = msg.messageId,
        )
        return true
    }
    suspend fun onTelegramRejectCommand(msg: TgMessage): Boolean {
        val firstReply = ZixaMCRequests.getFirstReply(msg)
        if (ConfigManager.CONFIG == null || firstReply.forwardFrom == null) return false
        if (ConfigManager.CONFIG!!.textOnReject4Target.isNotEmpty()) bot.sendMessage(
            ConfigManager.CONFIG!!.targetChatId,
            ConfigManager.CONFIG!!.textOnReject4Target,
            firstReply.messageId,
        )
        if (ConfigManager.CONFIG!!.textOnReject4User.isNotEmpty()) bot.sendMessage(
            firstReply.forwardFrom.id,
            ConfigManager.CONFIG!!.textOnReject4User,
        )
        return true
    }
}