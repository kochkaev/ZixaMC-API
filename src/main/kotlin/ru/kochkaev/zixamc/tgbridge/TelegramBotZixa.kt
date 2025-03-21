package ru.kochkaev.zixamc.tgbridge

import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.slf4j.Logger
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.*
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import java.io.FileOutputStream
import java.io.InputStream
import java.time.Duration

/**
 * @author vanutp
 */
class TelegramBotZixa(botApiUrl: String, val botToken: String, private val logger: Logger, private val POLL_TIMEOUT_SECONDS: Int = 60) {

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
    private val typedCallbackQueryHandlers: HashMap<String, suspend (TgCallbackQuery, TgCallback<out CallbackData>) -> TgCBHandlerResult> = hashMapOf()
    private val chatJoinRequestHandlers: MutableList<suspend (TgChatJoinRequest) -> Unit> = mutableListOf()
    lateinit var me: TgUser
        private set


    fun registerMessageHandler(handler: suspend (TgMessage) -> Unit) {
        messageHandlers.add(handler)
    }
    fun registerCallbackQueryHandler(handler: suspend (TgCallbackQuery) -> Unit) {
        callbackQueryHandlers.add(handler)
    }
    fun <T: CallbackData> registerCallbackQueryHandler(type: String, handler: suspend (TgCallbackQuery, TgCallback<T>) -> TgCBHandlerResult) {
        @Suppress("UNCHECKED_CAST")
        typedCallbackQueryHandlers[type] = handler as suspend (TgCallbackQuery, TgCallback<out CallbackData>) -> TgCBHandlerResult
    }
    fun registerChatJoinRequestHandlers(handler: suspend (TgChatJoinRequest) -> Unit) {
        chatJoinRequestHandlers.add(handler)
    }

    fun registerCommandHandler(command: String, handler: suspend (TgMessage) -> Unit) {
//        val cmdRegex = Regex("^/$command(@${me.username})?(\\s|\$)", RegexOption.IGNORE_CASE)
        val cmdRegex = Regex("^/$command(@${me.username})?(?:\\s+(.+))?\$", RegexOption.IGNORE_CASE)
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
                        update.message?.run {
                            SQLGroup.collectData(this.chat.id, this.from?.id)
                            var itCommand = false
                            for (handler in commandHandlers) {
                                itCommand = itCommand || handler.invoke(this)
                            }
                            if (!itCommand) messageHandlers.forEach {
                                it.invoke(this)
                            }
                        }
                        update.callbackQuery?.run {
                            SQLGroup.collectData(this.message.chat.id, this.from.id)
                            val sql = this.data?.toLongOrNull()?.let {
                                SQLCallback.get(it)
                            }
                            if (sql != null) {
                                typedCallbackQueryHandlers[sql.type]?.invoke(this, sql.callback)?.also { result ->
                                    if (result.deleteCallback) {
                                        if (result.deleteAllLinked) {
                                            if (result.deleteMarkup) editMessageReplyMarkup(
                                                chatId = this.message.chat.id,
                                                messageId = this.message.messageId,
                                                replyMarkup = TgReplyMarkup()
                                            )
                                            sql.linked.get()?.forEach { linked -> linked.getSQL()?.drop() }
                                        }
                                        sql.drop()
                                    }
                                }
                            }
                            else callbackQueryHandlers.forEach {
                                it.invoke(this)
                            }
                        }
                        update.chatJoinRequest?.run {
                            SQLGroup.collectData(this.chat.id, this.from.id)
                            chatJoinRequestHandlers.forEach {
                                it.invoke(this)
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
        okhttpClient.dispatcher.executorService.shutdown()
        okhttpClient.connectionPool.evictAll()
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
        protectContent: Boolean = false,
    ): TgMessage = call {
        client.sendMessage(TgSendMessageRequest(
            chatId = chatId,
            messageThreadId = messageThreadId,
            text = text,
            parseMode = parseMode,
            entities = entities,
            replyParameters = replyParameters,
            replyMarkup = replyMarkup,
            protectContent = protectContent,
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
    suspend fun stopPoll(
        chatId: Long,
        messageId: Int,
        businessConnectionId: String? = null,
        replyMarkup: TgInlineKeyboardMarkup? = null,
    ): TgPoll = call {
        client.stopPoll(TgStopPollRequest(
            businessConnectionId,
            chatId,
            messageId,
            replyMarkup,
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

    suspend fun banChatMember(chatId: Long, userId: Long) = call {
        client.banChatMember(TgBanChatMemberRequest(chatId, userId))
    }
    suspend fun unbanChatMember(chatId: Long, userId: Long, onlyIfBanned: Boolean) = call {
        client.unbanChatMember(TgUnbanChatMemberRequest(chatId, userId, onlyIfBanned))
    }

    suspend fun pinMessage(chatId: Long, messageId: Long, disableNotification: Boolean = false) = call {
        client.pinMessage(TgPinChatMessageRequest(chatId, messageId, disableNotification))
    }

    suspend fun approveChatJoinRequest(chatId: Long, userId: Long) = call {
        client.approveChatJoinRequest(TgApproveChatJoinRequest(chatId, userId))
    }

    suspend fun getFile(fileId: String) = call {
        client.getFile(TgGetFileRequest(fileId))
    }
    suspend fun downloadFile(fileUrl: String, localPatch: String) =
        saveFile(client.downloadFile(fileUrl).body(), localPatch)
    private fun saveFile(body: ResponseBody?, localPatch: String): String {
        if (body==null)
            return ""
        var input: InputStream? = null
        try {
            input = body.byteStream()
            //val file = File(getCacheDir(), "cacheFileAppeal.srl")
            val fos = FileOutputStream(localPatch)
            fos.use { output ->
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
            return localPatch
        }catch (e:Exception){
            ZixaMCTGBridge.logger.error("saveFile", e.toString())
        }
        finally {
            input?.close()
        }
        return ""
    }
    suspend fun saveFile(fileId: String, localPatch: String): String {
        val tgFile = getFile(fileId)
        val url = "https://api.telegram.org/file/bot$botToken/${tgFile.file_path?:return ""}"
        return downloadFile(url, localPatch)
    }
}
