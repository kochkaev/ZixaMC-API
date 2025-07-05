package ru.kochkaev.zixamc.api.telegram

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.slf4j.Logger
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.kochkaev.zixamc.api.ZixaMC
import ru.kochkaev.zixamc.api.config.GsonManager
import ru.kochkaev.zixamc.api.telegram.model.*
import ru.kochkaev.zixamc.api.sql.callback.CallbackData
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLProcess
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.process.ProcessorType
import java.io.FileOutputStream
import java.io.InputStream
import java.time.Duration
import java.util.LinkedList
import java.util.Queue

/**
 * @author vanutp
 */
class TelegramBotZixa(
    botApiUrl: String,
    val botToken: String,
    private val logger: Logger,
    private val POLL_TIMEOUT_SECONDS: Int = 60,
    val canAddToGroups: AccountType = AccountType.PLAYER,
) {

    private val okhttpClient = OkHttpClient.Builder()
        .readTimeout(Duration.ofSeconds((POLL_TIMEOUT_SECONDS + 10).toLong()))
        .build()
    private val client = Retrofit.Builder()
        .client(okhttpClient)
        .baseUrl("$botApiUrl/bot$botToken/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TgApi::class.java)
    var pollTask: Job? = null
    var postTask: Job? = null
    val postQueue: Queue<Pair<suspend () -> TgResponse<*>, CompletableDeferred<TgResponse<*>>>> = LinkedList()
    val postLock = Mutex()
    private val commandHandlers: MutableList<suspend (TgMessage) -> Boolean> = mutableListOf()
    private val messageHandlers: MutableList<suspend (TgMessage) -> Unit> = mutableListOf()
    private val callbackQueryHandlers: MutableList<suspend (TgCallbackQuery) -> Unit> = mutableListOf()
    val typedCallbackQueryHandlers: HashMap<String, suspend (TgCallbackQuery, SQLCallback<out CallbackData>) -> TgCBHandlerResult> = hashMapOf()
    private val chatJoinRequestHandlers: MutableList<suspend (TgChatJoinRequest) -> Unit> = mutableListOf()
    private val chatMemberUpdatedHandlers: MutableList<suspend (TgChatMemberUpdated) -> Unit> = mutableListOf()
    private val newChatMembersHandlers: MutableList<suspend (TgMessage) -> Unit> = mutableListOf()
    private val leftChatMemberHandlers: MutableList<suspend (TgMessage) -> Unit> = mutableListOf()
    private val botChatMemberUpdatedHandlers: MutableList<suspend (TgChatMemberUpdated) -> Unit> = mutableListOf()
    lateinit var me: TgUser
        private set


    fun registerMessageHandler(handler: suspend (TgMessage) -> Unit) {
        messageHandlers.add(handler)
    }
    fun registerCallbackQueryHandler(handler: suspend (TgCallbackQuery) -> Unit) {
        callbackQueryHandlers.add(handler)
    }
    fun <T: CallbackData> registerCallbackQueryHandler(type: String, model: Class<T>, handler: suspend (TgCallbackQuery, SQLCallback<T>) -> TgCBHandlerResult) {
        @Suppress("UNCHECKED_CAST")
        typedCallbackQueryHandlers[type] = handler as suspend (TgCallbackQuery, SQLCallback<out CallbackData>) -> TgCBHandlerResult
        SQLCallback.registries[type] = model
    }
    fun registerChatJoinRequestHandler(handler: suspend (TgChatJoinRequest) -> Unit) {
        chatJoinRequestHandlers.add(handler)
    }
    fun registerChatMemberUpdatedHandler(handler: suspend (TgChatMemberUpdated) -> Unit) {
        chatMemberUpdatedHandlers.add(handler)
    }
    fun registerBotChatMemberUpdatedHandler(handler: suspend (TgChatMemberUpdated) -> Unit) {
        botChatMemberUpdatedHandlers.add(handler)
    }
    fun registerNewChatMembersHandler(handler: suspend (TgMessage) -> Unit) {
        newChatMembersHandlers.add(handler)
    }
    fun registerLeftChatMemberHandler(handler: suspend (TgMessage) -> Unit) {
        leftChatMemberHandlers.add(handler)
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
        callWithoutDelay { client.deleteWebhook() }
        me = callWithoutDelay { client.getMe() }
    }

    suspend fun startPolling(scope: CoroutineScope) {
        if (pollTask != null) {
            throw IllegalStateException("polling already started")
        }
        pollTask = scope.launch {
            var offset = -1
            while (true) {
                try {
                    val updates = callWithoutDelay {
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
                            SQLGroup.collectData(this.chat, this.from)
                            var itCommand = false
                            var itSystem = false
                            var itProcess = false
                            if (this.migrateToChatId!=null && this.migrateFromChatId!=null){
                                itSystem = true
                                SQLGroup.get(this.migrateFromChatId)?.updateChatId(this.migrateToChatId)
                            }
                            else if (this.newChatMembers != null) {
//                                this.newChatMembers.forEach { new -> SQLGroup.collectData(this.chat.id, new.id) }
                                if (this.from?.let { from -> this.newChatMembers.contains(from) } == false)
                                    SQLGroup.collectData(this.chat, this.from)
                                newChatMembersHandlers.forEach { handler -> handler(this) }
                                itSystem = true
                            }
                            else if (this.leftChatMember != null) {
//                                SQLGroup.get(this.chat.id)?.members?.remove(LinkedUser(this.leftChatMember.id))
                                leftChatMemberHandlers.forEach { handler -> handler(this) }
                                itSystem = true
                            }
                            if (!itSystem) for (handler in commandHandlers) {
                                itCommand = itCommand || handler.invoke(this)
                            }
                            if (!itSystem && !itCommand) {
                                SQLProcess.getAll(this.chat.id).forEach { process ->
                                    when (process.type.processorType) {
                                        ProcessorType.REPLY_MESSAGE -> process.data?.also {
                                            if (it.messageId == this.replyToMessage?.messageId) {
                                                process as SQLProcess<Any>
                                                process.type.processor?.invoke(this, process, it)
                                                itProcess = true
                                            }
                                        }
                                        ProcessorType.ANY_MESSAGE -> process.data?.also {
                                            process as SQLProcess<Any>
                                            process.type.processor?.invoke(this, process, it)
                                            itProcess = true
                                        }
                                        else -> {}
                                    }
                                }
                            }
                            if (!itProcess && !itCommand && !itSystem) messageHandlers.forEach {
                                it.invoke(this)
                            }
                        }
                        update.callbackQuery?.run {
                            SQLGroup.collectData(this.message.chat, this.from)
                            val sql = this.data?.toLongOrNull()?.let {
                                SQLCallback.get(it)
                            }
                            if (sql != null) {
                                typedCallbackQueryHandlers[sql.type]?.invoke(this, sql)?.also { result ->
                                    if (result.deleteMarkup) try {
                                        editMessageReplyMarkup(
                                            chatId = this.message.chat.id,
                                            messageId = this.message.messageId,
                                            replyMarkup = TgReplyMarkup()
                                        )
                                    } catch (_: Exception) {}
                                    if (result.deleteAllLinked)
                                        sql.linked.get()?.forEach { sql -> sql.drop() }
                                    if (result.deleteCallback) sql.drop()
                                }
                            }
                            else callbackQueryHandlers.forEach {
                                it.invoke(this)
                            }
                        }
                        update.chatJoinRequest?.run {
                            SQLGroup.collectData(this.chat, this.from)
                            chatJoinRequestHandlers.forEach {
                                it.invoke(this)
                            }
                        }
                        update.chatMember?.run {
                            if (this.from.id != this.newChatMember.user.id)
                                SQLGroup.collectData(this.chat, this.from)
                            if (!(this.newChatMember.status == TgChatMemberStatuses.BANNED && (this.newChatMember as TgChatMemberBanned).untilDate == 0 || this.newChatMember.status == TgChatMemberStatuses.LEFT))
                                SQLGroup.get(this.chat.id)?.members?.remove(this.newChatMember.user.id)
                            chatMemberUpdatedHandlers.forEach {
                                it.invoke(this)
                            }
                        }
                        update.myChatMember?.run {
                            SQLGroup.collectData(this.chat, this.from)
                            botChatMemberUpdatedHandlers.forEach {
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
    suspend fun startPosting(scope: CoroutineScope) {
        if (postTask != null) {
            throw IllegalStateException("posting already started")
        }
        postTask = scope.launch {
            while(true) {
                try {
                    var post: (Pair<suspend () -> TgResponse<*>, CompletableDeferred<TgResponse<*>>>)? = null
                    postLock.withLock {
                        if (postQueue.isNotEmpty())
                            post = postQueue.poll()
                    }
                    post?.also { scope.launch {
                        try {
                            it.second.complete(it.first.invoke())
                        } catch (e: Exception) {
                            it.second.completeExceptionally(e)
                        }
                    } }
                    delay(60)
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
            logger.info("postTask finished")
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
        postTask?.cancelAndJoin()
        okhttpClient.dispatcher.executorService.shutdown()
        okhttpClient.connectionPool.evictAll()
    }


    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> call(f: suspend () -> TgResponse<T>): T {
        val deferred = CompletableDeferred<TgResponse<T>>()
        postLock.withLock {
            postQueue.add((f to deferred) as Pair<suspend () -> TgResponse<*>, CompletableDeferred<TgResponse<*>>>)
        }
        try {
            if (postTask == null) throw IllegalStateException("Post task is not started!")
            return deferred.await().result!!
        } catch (e: HttpException) {
            val resp = e.response() ?: throw e
            val body = GsonManager.gson.fromJson<TgResponse<T>>(resp.errorBody()?.string(), TgResponse::class.java)
            if (body.errorCode == 429) {
                delay((body.parameters?.retryAfter?:3L) * 1000L)
                return call(f)
            }
            else throw Exception("Telegram exception: ${resp.errorBody()?.string() ?: "no response body"}")
        }
    }
    private suspend fun <T> callWithoutDelay(f: suspend () -> TgResponse<T>): T {
        try {
            return f().result!!
        } catch (e: HttpException) {
            val resp = e.response() ?: throw e
            val body = GsonManager.gson.fromJson<TgResponse<T>>(resp.errorBody()?.string(), TgResponse::class.java)
            if (body.errorCode == 429) {
                delay((body.parameters?.retryAfter?:3L) * 1000L)
                return call(f)
            }
            else throw Exception("Telegram exception: ${resp.errorBody()?.string() ?: "no response body"}")
        }
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        messageThreadId: Int? = null,
        parseMode: String = "HTML",
        entities: List<TgEntity>? = null,
        replyParameters: TgReplyParameters? = null,
        replyMarkup: ITgMenu? = null,
        protectContent: Boolean = false,
    ): TgMessage = call {
        val markup = if (replyMarkup is TgMenu?) replyMarkup?.inlineAndId(chatId) else replyMarkup as TgReplyMarkup? to listOf()
        val result = client.sendMessage(TgSendMessageRequest(
            chatId = chatId,
            messageThreadId = messageThreadId,
            text = text,
            parseMode = parseMode,
            entities = entities,
            replyParameters = replyParameters,
            replyMarkup = markup?.first,
            protectContent = protectContent,
        ))
        if (result.ok) markup?.second?.forEach { SQLCallback.get(it)!!.messageId = result.result!!.messageId }
        else markup?.second?.forEach { SQLCallback.get(it)!!.drop() }
        result
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
        replyMarkup: ITgMenu? = null,
    ): TgPoll = call {
        val markup = if (replyMarkup is TgMenu) replyMarkup.inlineAndId(chatId) else (if (replyMarkup is TgInlineKeyboardMarkup) replyMarkup else null) to listOf()
        val result = client.stopPoll(
            TgStopPollRequest(
                businessConnectionId,
                chatId,
                messageId,
                markup.first,
            )
        )
        if (result.ok) markup.second.forEach { SQLCallback.get(it)!!.messageId = messageId }
        else markup.second.forEach { SQLCallback.get(it)!!.drop() }
        result
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
        client.editMessageText(
            TgEditMessageRequest(
                chatId,
                messageId,
                text,
                parseMode,
                disableWebPagePreview,
                entities = entities
            )
        )
    }

    suspend fun editMessageReplyMarkup(
        chatId: Long,
        messageId: Int,
        replyMarkup: ITgMenu,
    ) : TgMessage = call {
        val markup = if (replyMarkup is TgMenu) replyMarkup.inlineAndId(chatId) else replyMarkup as TgReplyMarkup to listOf()
        val result = client.editMessageReplyMarkup(TgEditMessageReplyMarkupRequest(
            chat_id = chatId,
            message_id = messageId,
            reply_markup = markup.first,
        ))
        if (result.ok) markup.second.forEach { SQLCallback.get(it)!!.messageId = result.result!!.messageId }
        else markup.second.forEach { SQLCallback.get(it)!!.drop() }
        result
    }
//    suspend fun editMessageReplyMarkup(
//        inlineMessageId: String,
//        replyMarkup: ITgMenu,
//    ) : TgMessage = call {
//        val markup = if (replyMarkup is TgMenu) replyMarkup.inlineAndId() else replyMarkup as TgReplyMarkup to listOf()
//        val result = client.editMessageReplyMarkup(TgEditMessageReplyMarkupRequest(
//            inline_message_id = inlineMessageId,
//            reply_markup = markup.first,
//        ))
//        if (result.ok) markup.second.forEach { SQLCallback.get(it)!!.messageId = result.result!!.messageId }
//        else markup.second.forEach { SQLCallback.get(it)!!.drop() }
//        result
//    }

    suspend fun deleteMessage(chatId: Long, messageId: Int) = call {
        client.deleteMessage(TgDeleteMessageRequest(chatId, messageId))
    }

    suspend fun banChatMember(chatId: Long, userId: Long) = call {
        client.banChatMember(TgBanChatMemberRequest(chatId, userId))
    }
    suspend fun unbanChatMember(chatId: Long, userId: Long, onlyIfBanned: Boolean) = call {
        client.unbanChatMember(TgUnbanChatMemberRequest(chatId, userId, onlyIfBanned))
    }
    suspend fun getChatMember(chatId: Long, userId: Long) = call {
        client.getChatMember(TgGetChatMemberRequest(chatId, userId))
    }
    suspend fun getChatMemberCount(chatId: Long) = call {
        client.getChatMemberCount(TgGetChatMemberCountRequest(chatId))
    }

    suspend fun pinMessage(chatId: Long, messageId: Long, disableNotification: Boolean = false) = call {
        client.pinMessage(
            TgPinChatMessageRequest(
                chatId,
                messageId,
                disableNotification
            )
        )
    }

    suspend fun approveChatJoinRequest(chatId: Long, userId: Long) = call {
        client.approveChatJoinRequest(TgApproveChatJoinRequest(chatId, userId))
    }

    suspend fun leaveChat(chatId: Long) = call {
        client.leaveChat(TgLeaveChatRequest(chatId))
    }

    suspend fun getChat(chatId: Long) = call {
        client.getChat(TgGetChatRequest(chatId))
    }

    suspend fun answerCallbackQuery(callbackQueryId: String, text: String? = null, showAlert: Boolean = false, url: String? = null) = call {
        client.answerCallbackQuery(
            TgAnswerCallbackQueryRequest(
                callbackQueryId,
                text,
                showAlert,
                url
            )
        )
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
            ZixaMC.logger.error("saveFile", e.toString())
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
