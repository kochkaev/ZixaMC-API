package ru.kochkaev.zixamc.requests

import kotlinx.coroutines.*
import retrofit2.Retrofit
import ru.kochkaev.zixamc.requests.ZixaMCRequests.Companion.logger
import ru.kochkaev.zixamc.requests.dataclassSQL.MinecraftAccountData
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
        bot = TelegramBotZixa(config.botAPIURL, config.botToken, logger, config.pollTimeout)
        runBlocking {
            bot.init()
        }
        bot.registerMessageHandler(this::onTelegramMessage)
        bot.registerCallbackQueryHandler(this::onTelegramCallbackQuery)
        bot.registerChatJoinRequestHandlers(this::onTelegramChatJoinRequest)
        bot.registerCommandHandler("accept", this::onTelegramAcceptCommand)
        bot.registerCommandHandler("reject", this::onTelegramRejectCommand)
        bot.registerCommandHandler("promote", this::onTelegramPromoteCommand)
        bot.registerCommandHandler("kick", this::onTelegramKickCommand)
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
                            } else if (MySQLIntegration.isNicknameNotAvailableToTake(entity.user_id, msg.text)) {
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
//                        bot.editMessageReplyMarkup(
//                            chatId = msg.chat.id,
//                            messageId = msg.replyToMessage.messageId,
//                            replyMarkup = TgReplyMarkup()
//                        )
                    }
                    "pending" -> {
//                        val firstReply = msg.replyToMessage?:return
//                        if (firstReply.from?.id == bot.me.id && firstReply.forwardOrigin != null) {
//                            val forwardedMessage = bot.forwardMessage(
//                                chatId = config.targetChatId,
//                                messageThreadId = config.targetTopicId,
//                                fromChatId = msg.chat.id,
//                                messageId = msg.messageId,
//                            )
//                            entity.addToTempArray(forwardedMessage.messageId.toString())
//                        }
                        val forwardedMessage = bot.forwardMessage(
                            chatId = config.targetChatId,
                            messageThreadId = config.targetTopicId,
                            fromChatId = msg.chat.id,
                            messageId = msg.messageId,
                        )
                        entity.addToTempArray(forwardedMessage.messageId.toString())
                    }
                }
            }
        }
        else {
            val replied = msg.replyToMessage?:return
            val entity = MySQLIntegration.getLinkedEntityByTempArrayMessagesId(replied.messageId.toLong())?:return
            if (!entity.temp_array!!.contains(replied.messageId.toString()) || !entity.getRequesterData()!!.requests.any {it.request_status == "pending"}) return
            bot.forwardMessage(
                chatId = entity.user_id,
                fromChatId = msg.chat.id,
                messageId = msg.messageId,
            )
            entity.addToTempArray(msg.messageId.toString())
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
            }
            "cancel_request" -> cancelRequest(entity)
            "cancel_sending_request" -> cancelSendingRequest(entity)
            "create_request" -> newRequest(entity)
            "send_request" -> {
                val request = entity.getRequesterData()!!.requests.first {it.request_status == "creating"}
                val forwardedMessage = bot.forwardMessage(
                    chatId = config.targetChatId,
                    messageThreadId = config.targetTopicId,
                    fromChatId = cbq.from.id,
                    messageId = request.message_id_in_chat_with_user.toInt()
                )
                val newMessage = bot.sendMessage(
                    chatId = config.targetChatId,
                    text = config.text.textOnSend4Target,
                    replyParameters = TgReplyParameters(forwardedMessage.messageId),
                )
                if (config.poll.autoCreatePoll) {
                    val poll = bot.sendPoll(
                        chatId = config.targetChatId,
                        messageThreadId = config.targetTopicId,
                        question = config.poll.pollQuestion.replace("{nickname}", "${request.request_nickname}"),
                        options = listOf(
                            TgInputPollOption(config.poll.pollAnswerTrue),
                            TgInputPollOption(config.poll.pollAnswerNull),
                            TgInputPollOption(config.poll.pollAnswerFalse),
                        ),
                        replyParameters = TgReplyParameters(
                            message_id = forwardedMessage.messageId,
                        ),
                    )
                    entity.addToTempArray(poll.messageId.toString())
                }
                bot.pinMessage(config.targetChatId, forwardedMessage.messageId.toLong(), true)
                bot.sendMessage(
                    chatId = cbq.from.id,
                    text = config.text.textOnSend4User,
                    replyParameters = TgReplyParameters(cbq.message.messageId),
                )
                request.message_id_in_target_chat = forwardedMessage.messageId.toLong()
                entity.addToTempArray(forwardedMessage.messageId.toString())
                entity.addToTempArray(newMessage.messageId.toString())
                request.request_status = "pending"
                entity.editRequest(request)
                MySQLIntegration.setNickname(entity.user_id, request.request_nickname!!)
            }
        }
        bot.editMessageReplyMarkup(
            chatId = cbq.message.chat.id,
            messageId = cbq.message.messageId,
            replyMarkup = TgReplyMarkup()
        )
    }

    suspend fun onTelegramChatJoinRequest(request: TgChatJoinRequest) {
        val entity = MySQLIntegration.getLinkedEntity(request.from.id)?:return
        if (entity.account_type<=1) {
            bot.approveChatJoinRequest(request.chat.id, request.from.id)
        }
    }

    suspend fun onTelegramAcceptCommand(msg: TgMessage): Boolean {
        if (msg.chat.id >= 0 || !MySQLIntegration.isAdmin(msg.from?.id?:return false)) return true
        val replied = msg.replyToMessage?:return false
//        if (replied.from?.id != bot.me.id || replied.forwardOrigin == null) return false
        val entity = MySQLIntegration.getLinkedEntityByTempArrayMessagesId(replied.messageId.toLong())?:return false
        val request = entity.getRequesterData()!!.requests.firstOrNull {it.request_status == "pending"} ?: return false
        bot.sendMessage(
            chatId = config.targetChatId,
            text = config.text.textOnAccept4Target.replace("{nickname}", "${request.request_nickname}"),
            replyParameters = TgReplyParameters(replied.messageId),
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
        entity.temp_array = arrayOf()
        entity.addMinecraftAccount(MinecraftAccountData(request.request_nickname!!, "player"))
        ZixaMCRequests.addToWhitelist(request.request_nickname!!)
        return true
    }
    suspend fun onTelegramRejectCommand(msg: TgMessage): Boolean {
        if (msg.chat.id >= 0 || !MySQLIntegration.isAdmin(msg.from?.id?:return false)) return true
        val replied = msg.replyToMessage?:return false
        if (replied.from?.id != bot.me.id || replied.forwardOrigin == null) return false
        val entity = MySQLIntegration.getLinkedEntityByTempArrayMessagesId(replied.messageId.toLong())?:return false
        val request = entity.getRequesterData()!!.requests.firstOrNull {it.request_status == "pending"} ?: return false
        bot.sendMessage(
            chatId = config.targetChatId,
            text = config.text.textOnReject4Target.replace("{nickname}", "${request.request_nickname}"),
            replyParameters = TgReplyParameters(replied.messageId),
        )
        bot.sendMessage(
            chatId = entity.user_id,
            text = config.text.textOnReject4User,
            replyParameters = TgReplyParameters(request.message_id_in_chat_with_user.toInt()),
        )
        request.request_status = "rejected"
        entity.editRequest(request)
        entity.temp_array = arrayOf()
        return true
    }
    suspend fun onTelegramPromoteCommand(msg: TgMessage): Boolean {
        if (!MySQLIntegration.isAdmin(msg.from?.id?:return false)) return true
        val args = msg.text?.split(" ")?:return false
        val isNotArgsPassed = args.size > 1
        val isArgUserId = if (isNotArgsPassed) args[1].matches("[0-9]+".toRegex()) && args[1].length == 10 else false
        val isReplyToMessage = msg.replyToMessage != null && args.size == 2
        val isArgTargetId = if (isNotArgsPassed) args[if (isReplyToMessage) 1 else 2].matches("[0-9]+".toRegex()) else false
        if (!(args.size == 3 && !isReplyToMessage || isReplyToMessage && args.size == 2) || !promoteUser(
            userId =
                if (isArgUserId) args[1].toLong()
//                else if (args[2].startsWith("@")) msg.entities?.firstOrNull {it.type == "mention" }?.
                else if (isReplyToMessage) msg.replyToMessage!!.from?.id
                else null,
            nickname = if (args[1].matches("[a-zA-Z0-9_]+".toRegex()) && args[1].length in 3..16 && !isArgUserId) args[1] else null,
            targetName = if (!isArgTargetId) args[if (isReplyToMessage) 1 else 2] else null,
            argTargetId = if (isArgTargetId) args[if (isReplyToMessage) 1 else 2].toInt() else null,
        )) {
            bot.sendMessage(
                chatId = msg.chat.id,
                messageThreadId = msg.messageThreadId,
                text = config.text.textSyntaxPromoteHelp,
                replyParameters = TgReplyParameters(msg.messageId),
            )
            return false
        } else {
            bot.sendMessage(
                chatId = msg.chat.id,
                messageThreadId = msg.messageThreadId,
                text = config.text.textOnPromote4Target,
                replyParameters = TgReplyParameters(msg.messageId),
            )
            return true
        }
    }
    suspend fun onTelegramKickCommand(msg: TgMessage): Boolean {
        if (!MySQLIntegration.isAdmin(msg.from?.id?:return false)) return true
        val args = msg.text?.split(" ")?:return false
        val isArgUserId = if (args.size>1) args[1].matches("[0-9]+".toRegex()) && args[1].length == 10 else false
        val isReplyToMessage = msg.replyToMessage != null
        val entity =
            if (isArgUserId) MySQLIntegration.getLinkedEntity(args[1].toLong())
            else if (isReplyToMessage) MySQLIntegration.getLinkedEntity(msg.replyToMessage!!.from?.id?:return false)
            else if (args[1].matches("[a-zA-Z0-9_]+".toRegex()) && args[1].length in 3..16) MySQLIntegration.getLinkedEntityByNickname(args[1])
            else null
        if (entity == null || !promoteUser(
            argEntity = entity,
            argTargetId = 2,
        )) {
            bot.sendMessage(
                chatId = msg.chat.id,
                messageThreadId = msg.messageThreadId,
                text = config.text.textSyntaxKickHelp,
                replyParameters = TgReplyParameters(msg.messageId),
            )
            return false
        } else {
            bot.sendMessage(
                chatId = msg.chat.id,
                messageThreadId = msg.messageThreadId,
                text = config.text.textOnKick4Target.replace("{nickname}", entity.nickname ?: entity.user_id.toString()),
                replyParameters = TgReplyParameters(msg.messageId),
            )
            bot.banChatMember(msg.chat.id, entity.user_id)
            entity.nicknames?.forEach { ZixaMCRequests.removeFromWhitelist(it) }
            try {
                bot.sendMessage(
                    chatId = entity.user_id,
                    text = config.text.textOnKick4User,
                    replyParameters = TgReplyParameters(msg.messageId),
                )
            } catch (_: Exception) {}
            return true
        }
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

    suspend fun cancelRequest(entity: SQLEntity): Boolean {
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
        entity.temp_array = arrayOf()
        return true
    }
    suspend fun cancelSendingRequest(entity: SQLEntity): Boolean {
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
    suspend fun newRequest(entity: SQLEntity): Boolean {
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

    fun promoteUser(argEntity: SQLEntity? = null, userId: Long? = null, nickname: String? = null, targetName: String? = null, argTargetId: Int? = null): Boolean {
        val entity = argEntity ?:
            if (userId != null) MySQLIntegration.getLinkedEntity(userId) ?: return false
            else if (nickname != null) MySQLIntegration.getLinkedEntityByNickname(nickname) ?: return false
            else return false
        val targetId = argTargetId ?: when (targetName?.lowercase()?:return false) {
            "admin" -> 0
            "player" -> 1
            "requester" -> 2
            else -> 3
        }
        entity.promote(targetId)
        return entity.account_type==targetId
    }
}