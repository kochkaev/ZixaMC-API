package ru.kochkaev.zixamc.requests

import kotlinx.coroutines.*
import ru.kochkaev.zixamc.requests.ZixaMCRequests.Companion.logger
import ru.kochkaev.zixamc.requests.dataclassSQL.RequestData
import ru.kochkaev.zixamc.requests.dataclassTelegram.*

/**
 * @author kochkaev
 */
object RequestsBot {
    lateinit var bot: TelegramBotZixa
    private lateinit var config: Config.RequestsBotDataClass
    private val coroutineScope = CoroutineScope(Dispatchers.IO).plus(SupervisorJob())

    fun startBot() {
        config = ConfigManager.CONFIG!!.requestsBot
        bot = TelegramBotZixa(config.botAPIURL, config.botToken, logger)
        runBlocking {
            bot.init()
        }
        bot.registerMessageHandler(this::onTelegramMessage)
        bot.registerCallbackQueryHandler(this::onTelegramCallbackQuery)
        bot.registerCommandHandler("accept", this::onTelegramAcceptCommand)
        bot.registerCommandHandler("reject", this::onTelegramRejectCommand)
        bot.registerCommandHandler("start", this::onTelegramStartCommand)
        bot.registerCommandHandler("new", this::onTelegramNewCommand)
        bot.registerCommandHandler("cancel", this::onTelegramCancelCommand)
        coroutineScope.launch {
            bot.startPolling(coroutineScope)
        }
    }
    fun stopBot() {
        coroutineScope.launch {
            bot.shutdown()
        }
    }

    suspend fun onTelegramMessage(msg: TgMessage) {
        if (msg.chat.id>=0) {
            val entity = MySQLIntegration.getLinkedEntity(msg.from!!.id)?:return
            if (entity.account_type == 2) {
                val requesterData = entity.getRequesterData()?:return
                if (!requesterData.agreed_with_rules) {
                    bot.sendMessage(
                        msg.chat.id,
                        config.text.textMustAgreeWithRules,
                    )
                    return
                }
                val requests = requesterData.requests
                val it = requests.first { !listOf("accepted", "rejected", "canceled").contains(it.request_status) }
                when (it.request_status) {
                    "creating" -> if (it.message_id_in_chat_with_user == (msg.replyToMessage?.messageId?:return).toLong()) {
                        val newMessage: TgMessage
                        if (it.request_nickname == null) {
                            if ((msg.text?.length ?: return) !in 3..16 || !msg.text.matches(Regex("[a-zA-Z0-9_]+"))) {
                                newMessage = bot.sendMessage(
                                    chatId = msg.chat.id,
                                    text = config.text.textWrongNickname,
                                    replyParameters = TgReplyParameters(msg.messageId),
                                    replyMarkup = TgForceReply(
                                        true,
                                        config.text.textInputFieldPlaceholderNickname.ifEmpty { null }
                                    )
                                )
                            } else if (MySQLIntegration.isNicknameTaken(msg.text)) {
                                newMessage = bot.sendMessage(
                                    chatId = msg.chat.id,
                                    text = config.text.textTakenNickname,
                                    replyParameters = TgReplyParameters(msg.messageId),
                                    replyMarkup = TgForceReply(
                                        true,
                                        config.text.textInputFieldPlaceholderNickname.ifEmpty { null }
                                    )
                                )
                            } else {
                                newMessage = bot.sendMessage(
                                    chatId = msg.chat.id,
                                    text = config.text.textOnNewRequest,
                                    replyParameters = TgReplyParameters(msg.messageId),
                                    replyMarkup = TgForceReply(
                                        true,
                                        config.text.textInputFieldPlaceholderRequest.ifEmpty { null }
                                    )
                                )
                                it.request_nickname = msg.text
                            }
                            it.message_id_in_chat_with_user = newMessage.messageId.toLong()
                        }
                        else {
                            newMessage = bot.sendMessage(
                                chatId = msg.chat.id,
                                text = config.text.textConfirmSendRequest,
                                replyParameters = TgReplyParameters(msg.messageId),
                                replyMarkup = TgInlineKeyboardMarkup(
                                    listOf(
                                        listOf(
                                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                                text = config.text.textButtonConfirmSending,
                                                callback_data = "send_request"
                                            ),
                                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                                text = config.text.textButtonCancelRequest,
                                                callback_data = "cancel_sending_request"
                                            ),
                                        )
                                    )
                                )
                            )
                            it.message_id_in_chat_with_user = msg.messageId.toLong()
                        }
                        entity.editRequest(it)
                    }
                    "pending" -> {
                        val firstReply = ZixaMCRequests.getFirstReply(msg)
                        if (firstReply.from?.id == bot.me.id && firstReply.forwardOrigin != null) {
                            bot.forwardMessage(
                                chatId = config.targetChatId,
                                messageThreadId = config.targetTopicId,
                                fromChatId = msg.chat.id,
                                messageId = msg.messageId,
                            )
                        }
                    }
                }
            }
        }
        else {
            val firstReply = ZixaMCRequests.getFirstReply(msg)
            if (firstReply.from?.id != bot.me.id || firstReply.forwardOrigin == null) return
            val entity = MySQLIntegration.getLinkedEntityByUserPendingRequestTargetMessageId(firstReply.messageId.toLong())?:return
            if (!entity.getRequesterData()!!.requests.any {it.request_status == "pending"}) return
            bot.forwardMessage(
                chatId = entity.user_id,
                fromChatId = msg.chat.id,
                messageId = msg.messageId,
            )
        }
    }
    suspend fun onTelegramCallbackQuery(cbq: TgCallbackQuery) {
        val entity = MySQLIntegration.getLinkedEntity(cbq.from.id)?:return
        when (cbq.data) {
            "agree_with_rules" -> {
                MySQLIntegration.setAgreedWithRules(cbq.from.id, true)
                val requests = entity.getRequesterData()?.requests?:return
                if (requests.any {it.request_status == "creating"}) {
                    val editedRequest = requests.first{it.request_status == "creating"}
                    val newMessage = bot.sendMessage(
                        chatId = cbq.from.id,
                        text = config.text.textNeedNickname,
                        replyMarkup = TgForceReply(
                            true,
                            config.text.textInputFieldPlaceholderNickname.ifEmpty { null }
                        )
                    )
                    editedRequest.message_id_in_chat_with_user = newMessage.messageId.toLong()
                    entity.editRequest(editedRequest)
                }
                bot.editMessageReplyMarkup(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgReplyMarkup()
                )
            }
            "redraw_request" -> {
                entity.data = MySQLIntegration.modifyData(
                    data = entity.data,
                    accountType = entity.account_type,
                    insertionAccountTypeLevel = 2,
                    insertField = "requests",
                    insertData = entity.getRequesterData()!!.requests.filter {it.request_status != "creating"},
                )
                newRequest(entity)
                bot.editMessageReplyMarkup(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgReplyMarkup()
                )
            }
            "cancel_request" -> {
                cancelRequest(entity)
                bot.editMessageReplyMarkup(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgReplyMarkup()
                )
            }
            "cancel_sending_request" -> {
                cancelSendingRequest(entity)
                bot.editMessageReplyMarkup(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgReplyMarkup()
                )
            }
            "create_request" -> {
                newRequest(entity)
                bot.editMessageReplyMarkup(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgReplyMarkup()
                )
            }
            "send_request" -> {
                val request = entity.getRequesterData()!!.requests.first {it.request_status == "creating"}
                val forwardedMessage = bot.forwardMessage(
                    chatId = config.targetChatId,
                    messageThreadId = config.targetTopicId,
                    fromChatId = cbq.from.id,
                    messageId = request.message_id_in_chat_with_user.toInt()
                )
                bot.sendMessage(
                    chatId = config.targetChatId,
                    text = config.text.textOnSend4Target,
                    replyParameters = TgReplyParameters(forwardedMessage.messageId),
                )
                if (config.poll.autoCreatePoll) bot.sendPoll(
                    chatId = config.targetChatId,
                    messageThreadId = config.targetTopicId,
                    question = config.poll.pollQuestion.replace("{nickname}", "@${request.request_nickname}"),
                    options = listOf(
                        TgInputPollOption(config.poll.pollAnswerTrue),
                        TgInputPollOption(config.poll.pollAnswerNull),
                        TgInputPollOption(config.poll.pollAnswerFalse),
                    ),
                    replyParameters = TgReplyParameters(
                        message_id = forwardedMessage.messageId,
                    ),
                )
                bot.sendMessage(
                    chatId = cbq.from.id,
                    text = config.text.textOnSend4User,
                    replyParameters = TgReplyParameters(cbq.message.messageId),
                )
                request.message_id_in_target_chat = forwardedMessage.messageId.toLong()
                entity.pending_request_target_message_id = forwardedMessage.messageId
                request.request_status = "pending"
                entity.editRequest(request)
                MySQLIntegration.setNickname(entity.user_id, request.request_nickname!!)
                bot.editMessageReplyMarkup(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgReplyMarkup()
                )
            }
        }
    }

    suspend fun onTelegramAcceptCommand(msg: TgMessage): Boolean {
        if (msg.chat.id >= 0 || MySQLIntegration.isAdmin(msg.from?.id?:return false)) return true
        val firstReply = ZixaMCRequests.getFirstReply(msg)
        if (firstReply.from?.id != bot.me.id || firstReply.forwardOrigin == null) return false
        val entity = MySQLIntegration.getLinkedEntityByUserPendingRequestTargetMessageId(firstReply.messageId.toLong())?:return false
        val request = entity.getRequesterData()!!.requests.firstOrNull {it.request_status == "pending"} ?: return false
        bot.sendMessage(
            chatId = config.targetChatId,
            text = config.text.textOnAccept4Target,
            replyParameters = TgReplyParameters(firstReply.messageId),
        )
        bot.sendMessage(
            chatId = entity.user_id,
            text = config.text.textOnAccept4User,
            replyParameters = TgReplyParameters(request.message_id_in_chat_with_user.toInt()),
            replyMarkup = TgInlineKeyboardMarkup(listOf(listOf(
                TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.textButtonJoinToPlayersGroup,
                    url = config.playersGroupInviteLink
                )
            )))
        )
        request.request_status = "accepted"
        entity.editRequest(request)
        entity.promote(1)
        entity.pending_request_target_message_id = null
        ZixaMCRequests.addToWhitelist(request.request_nickname!!)
        return true
    }
    suspend fun onTelegramRejectCommand(msg: TgMessage): Boolean {
        if (msg.chat.id >= 0 || MySQLIntegration.isAdmin(msg.from?.id?:return false)) return true
        val firstReply = ZixaMCRequests.getFirstReply(msg)
        if (firstReply.from?.id != bot.me.id || firstReply.forwardOrigin == null) return false
        val entity = MySQLIntegration.getLinkedEntityByUserPendingRequestTargetMessageId(firstReply.messageId.toLong())?:return false
        val request = entity.getRequesterData()!!.requests.firstOrNull {it.request_status == "pending"} ?: return false
        bot.sendMessage(
            chatId = config.targetChatId,
            text = config.text.textOnReject4Target,
            replyParameters = TgReplyParameters(firstReply.messageId),
        )
        bot.sendMessage(
            chatId = entity.user_id,
            text = config.text.textOnReject4User,
            replyParameters = TgReplyParameters(request.message_id_in_chat_with_user.toInt()),
        )
        request.request_status = "rejected"
        entity.editRequest(request)
        entity.pending_request_target_message_id = null
        return true
    }
    suspend fun onTelegramStartCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        MySQLIntegration.addUser(msg.from?.id?:return false)
        bot.sendMessage(
            chatId = msg.chat.id,
            text = config.text.textOnStart,
            replyMarkup = TgInlineKeyboardMarkup(listOf(listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                text = config.text.textButtonCreateRequest,
                callback_data = "create_request",
            ))))
        )
        return true
    }
    suspend fun onTelegramNewCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        if (msg.from == null) return false
        val entity = MySQLIntegration.getLinkedEntity(msg.from.id)?:return false
        return newRequest(entity)
    }
    suspend fun onTelegramCancelCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        val entity = MySQLIntegration.getLinkedEntity(msg.from?.id?:return false)?:return false
        val requests = (entity.getRequesterData()?:return false).requests
        if (requests.any {it.request_status == "pending"}) return cancelRequest(entity)
        else if (requests.any {it.request_status == "creating"}) return cancelSendingRequest(entity)
        return false
    }

    private suspend fun cancelRequest(entity: SQLEntity): Boolean {
        entity.editRequest((entity.getRequesterData()?:return false).requests.first { it.request_status == "pending" }.apply { this.request_status = "canceled" })
        bot.sendMessage(
            chatId = entity.user_id,
            text = config.text.textRequestCanceled4User,
            replyMarkup = TgInlineKeyboardMarkup(
                inline_keyboard = listOf(listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.textButtonCreateRequest,
                    callback_data = "create_request",
                )))
            )
        )
        bot.sendMessage(
            chatId = config.targetChatId,
            messageThreadId = config.targetTopicId,
            text = config.text.textRequestCanceled4Target,
        )
        return true
    }
    private suspend fun cancelSendingRequest(entity: SQLEntity): Boolean {
        entity.data = MySQLIntegration.modifyData(
            data = entity.data,
            accountType = entity.account_type,
            insertionAccountTypeLevel = 2,
            insertField = "requests",
            insertData = (entity.getRequesterData()?:return false).requests.filter { it.request_status == "creating" }
        )
        bot.sendMessage(
            chatId = entity.user_id,
            text = config.text.textRequestCanceled4User,
            replyMarkup = TgInlineKeyboardMarkup(
                inline_keyboard = listOf(listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.textButtonCreateRequest,
                    callback_data = "create_request",
                )))
            )
        )
        return true
    }
    private suspend fun newRequest(entity: SQLEntity): Boolean {
        when (entity.getOrCreateRequesterData().requests.firstOrNull { listOf("creating", "pending").contains(it.request_status) }?.request_status ?: "") {
            "creating" -> {
                bot.sendMessage(
                    chatId = entity.user_id,
                    text = config.text.textYouAreNowCreatingRequest,
                    replyMarkup = TgInlineKeyboardMarkup(
                        inline_keyboard = listOf(listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.text.textButtonRedrawRequest,
                            callback_data = "redraw_request",
                        )))
                    )
                )
                return false
            }
            "pending" -> {
                bot.sendMessage(
                    chatId = entity.user_id,
                    text = config.text.textYouHavePendingRequest,
                    replyMarkup = TgInlineKeyboardMarkup(
                        inline_keyboard = listOf(listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.text.textButtonCancelRequest,
                            callback_data = "cancel_request",
                        )))
                    )
                )
                return false
            }
        }
        if (entity.account_type<2) {
            bot.sendMessage(
                chatId = entity.user_id,
                text = config.text.textYouAreNowPlayer,
            )
            return false
        }
        val forReplyMessage = if (MySQLIntegration.isAgreedWithRules(entity.user_id)) bot.sendMessage(
            chatId = entity.user_id,
            text = config.text.textNeedNickname,
            replyMarkup = TgForceReply(
                true,
                config.text.textInputFieldPlaceholderNickname.ifEmpty { null }
            )
        )
        else bot.sendMessage(
            chatId = entity.user_id,
            text = config.text.textNeedAgreeWithRules,
            replyMarkup = TgInlineKeyboardMarkup(
                listOf(listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.textButtonAgreeWithRules,
                    callback_data = "agree_with_rules",
                ))),
            )
        )
        MySQLIntegration.addRequest(entity.user_id, RequestData(
            null,
            forReplyMessage.messageId.toLong(),
            "creating",
            null,
        ))
        return true
    }
}