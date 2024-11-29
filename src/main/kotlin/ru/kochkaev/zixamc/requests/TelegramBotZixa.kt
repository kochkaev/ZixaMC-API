package ru.kochkaev.zixamc.requests

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import org.slf4j.Logger
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.kochkaev.zixamc.requests.dataclassTelegram.*
import java.time.Duration

/**
 * @author vanutp
 */
class TelegramBotZixa(botApiUrl: String, botToken: String, private val logger: Logger, private val POLL_TIMEOUT_SECONDS: Int = 60) {

    private val okhttpClient = OkHttpClient.Builder()
        .readTimeout(Duration.ofSeconds((POLL_TIMEOUT_SECONDS + 10).toLong()))
        .build()
    private val client = Retrofit.Builder()
        .client(okhttpClient)
        .baseUrl("$botApiUrl/bot$botToken/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TgApi::class.java)
    private var pollTask: Job? = null
    private val commandHandlers: MutableList<suspend (TgMessage) -> Boolean> = mutableListOf()
    private val messageHandlers: MutableList<suspend (TgMessage) -> Unit> = mutableListOf()
    private val callbackQueryHandlers: MutableList<suspend (TgCallbackQuery) -> Unit> = mutableListOf()
    lateinit var me: TgUser
        private set


    fun registerMessageHandler(handler: suspend (TgMessage) -> Unit) {
        messageHandlers.add(handler)
    }
    fun registerCallbackQueryHandler(handler: suspend (TgCallbackQuery) -> Unit) {
        callbackQueryHandlers.add(handler)
    }

    fun registerCommandHandler(command: String, handler: suspend (TgMessage) -> Unit) {
        val cmdRegex = Regex("^/$command(@${me.username})?(\\s|\$)", RegexOption.IGNORE_CASE)
        commandHandlers.add {
            if (cmdRegex.matches(it.effectiveText ?: "")) {
                handler(it)
                return@add true
            } else {
                return@add false
            }
        }
    }

    suspend fun init() {
        call { client.deleteWebhook() }
        me = call { client.getMe() }
    }

    suspend fun startPolling(scope: CoroutineScope) {
        if (pollTask != null) {
            throw IllegalStateException("polling already started")
        }
        pollTask = scope.launch {
            var offset = -1
            while (true) {
                try {
                    val updates = call {
                        client.getUpdates(
                            offset,
                            timeout = POLL_TIMEOUT_SECONDS,
                        )
                    }
                    if (updates.isEmpty()) {
                        continue
                    }
                    offset = updates.last().updateId + 1
                    updates.forEach { update ->
                        when {
                            update.message != null -> {
                                for (handler in commandHandlers) {
                                    if (handler.invoke(update.message)) {
                                        return@forEach
                                    }
                                }
                                messageHandlers.forEach {
                                    it.invoke(update.message)
                                }
                            }
                            update.callbackQuery != null -> {
                                callbackQueryHandlers.forEach {
                                    it.invoke(update.callbackQuery)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    when (e) {
                        is CancellationException -> break
                        else -> {
                            logger.error(e.message.toString(), e)
                            delay(1000)
                        }
                    }
                }
            }
            logger.info("pollTask finished")
        }
    }

    suspend fun recoverPolling(scope: CoroutineScope) {
        val task = pollTask
        if (task != null) {
            if (!task.isCompleted) {
                task.cancelAndJoin()
            }
            pollTask = null
        }
        startPolling(scope)
    }

    suspend fun shutdown() {
        pollTask?.cancelAndJoin()
        okhttpClient.dispatcher().executorService().shutdown()
        okhttpClient.connectionPool().evictAll()
    }

    private suspend fun <T> call(f: suspend () -> TgResponse<T>): T {
        try {
            return f().result!!
        } catch (e: HttpException) {
            val resp = e.response() ?: throw e
            throw Exception("Telegram exception: ${resp.errorBody()?.string() ?: "no response body"}")
        }
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        messageThreadId: Int? = null,
        parseMode: String = "HTML",
        entities: List<TgEntity>? = null,
        replyParameters: TgReplyParameters? = null,
        replyMarkup: TgReplyMarkup? = null,
    ): TgMessage = call {
        client.sendMessage(TgSendMessageRequest(
            chatId = chatId,
            messageThreadId = messageThreadId,
            text = text,
            parseMode = parseMode,
            entities = entities,
            replyParameters = replyParameters,
            replyMarkup = replyMarkup,
        ))
    }

    suspend fun sendPoll(
        businessConnectionId: String? = null,
        chatId: Long,
        messageThreadId: Int? = null,
        question: String,
        questionParseMode: String = "HTML",
        questionEntities: List<TgEntity>? = null,
        options: List<TgInputPollOption>,
        isAnonymous: Boolean = false,
        type: String = "regular",
        allowsMultipleAnswers: Boolean = false,
        correctOptionId: Int? = null,
        explanation: String? = null,
        explanationParseMode: String = "HTML",
        explanationEntities: List<TgEntity>? = null,
        openPeriod: Int? = null,
        closeDate: Int? = null,
        isClosed: Boolean = false,
        disableNotification: Boolean = false,
        protectContent: Boolean = false,
        allowPaidBroadcast: Boolean = false,
        messageEffectId: String? = null,
        replyParameters: TgReplyParameters? = null,
    ): TgMessage = call {
        client.sendPoll(TgSendPollRequest(
            businessConnectionId,
            chatId,
            messageThreadId,
            question,
            questionParseMode,
            questionEntities,
            options,
            isAnonymous,
            type,
            allowsMultipleAnswers,
            correctOptionId,
            explanation,
            explanationParseMode,
            explanationEntities,
            openPeriod,
            closeDate,
            isClosed,
            disableNotification,
            protectContent,
            allowPaidBroadcast,
            messageEffectId,
            replyParameters
        ))
    }

    suspend fun forwardMessage(
        chatId: Long,
        messageThreadId: Int? = null,
        fromChatId: Long,
        disableNotification: Boolean = false,
        protectContent: Boolean = false,
        messageId: Int,
    ): TgMessage = call {
        client.forwardMessage(TgForwardMessageRequest(chatId, messageThreadId, fromChatId, disableNotification, protectContent, messageId))
    }

    suspend fun editMessageText(
        chatId: Long,
        messageId: Int,
        text: String,
        parseMode: String = "HTML",
        disableWebPagePreview: Boolean = true,
        entities: List<TgEntity>? = null,
    ): TgMessage = call {
        client.editMessageText(TgEditMessageRequest(chatId, messageId, text, parseMode, disableWebPagePreview, entities=entities))
    }

    suspend fun editMessageReplyMarkup(
        chatId: Long,
        messageId: Int,
        replyMarkup: TgReplyMarkup,
    ) : TgMessage = call {
        client.editMessageReplyMarkup(TgEditMessageReplyMarkupRequest(
            chat_id = chatId,
            message_id = messageId,
            reply_markup = replyMarkup,
        ))
    }
    suspend fun editMessageReplyMarkup(
        inlineMessageId: String,
        replyMarkup: TgReplyMarkup,
    ) : TgMessage = call {
        client.editMessageReplyMarkup(TgEditMessageReplyMarkupRequest(
            inline_message_id = inlineMessageId,
            reply_markup = replyMarkup,
        ))
    }

    suspend fun deleteMessage(chatId: Long, messageId: Int) = call {
        client.deleteMessage(TgDeleteMessageRequest(chatId, messageId))
    }
}
