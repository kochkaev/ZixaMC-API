package ru.kochkaev.zixamc.tgbridge.requests

import ru.kochkaev.zixamc.tgbridge.BotLogic
import ru.kochkaev.zixamc.tgbridge.MySQLIntegration
import ru.kochkaev.zixamc.tgbridge.RequestsBot.bot
import ru.kochkaev.zixamc.tgbridge.RequestsBot.config
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.AccountType
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.RequestType
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.cancelRequest
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.cancelSendingRequest
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.newRequest
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.*

object RequestsBotUpdateManager {
    suspend fun onTelegramMessage(msg: TgMessage) {
        if (msg.chat.id>=0) {
            val entity = MySQLIntegration.getLinkedEntity(msg.from!!.id)?:return
            if (entity.isRestricted) return
            if (entity.accountType == AccountType.REQUESTER) {
                val requesterData = entity.data?:return
                if (!entity.agreedWithRules) {
                    bot.sendMessage(
                        msg.chat.id,
                        config.user.lang.creating.mustAgreeWithRules,
                    )
                    return
                }
                val requests = requesterData.requests
                val it = requests.first { !RequestType.getAllDone().contains(it.request_status) }
                when (it.request_status) {
                    RequestType.CREATING -> if (it.message_id_in_chat_with_user == (msg.replyToMessage?.messageId?:return).toLong()) {
                        val newMessage: TgMessage
                        if (it.request_nickname == null) {
                            if ((msg.text?.length ?: return) !in 3..16 || !msg.text.matches(Regex("[a-zA-Z0-9_]+"))) {
                                newMessage = bot.sendMessage(
                                    chatId = msg.chat.id,
                                    text = BotLogic.escapePlaceholders(config.user.lang.creating.wrongNickname, msg.text),
                                    replyParameters = TgReplyParameters(msg.messageId),
                                    replyMarkup = TgForceReply(
                                        true,
                                        config.user.lang.inputField.enterNickname.ifEmpty { null }
                                    )
                                )
                            } else if (MySQLIntegration.isNicknameNotAvailableToTake(entity.userId, msg.text)) {
                                newMessage = bot.sendMessage(
                                    chatId = msg.chat.id,
                                    text = BotLogic.escapePlaceholders(config.user.lang.creating.takenNickname, msg.text),
                                    replyParameters = TgReplyParameters(msg.messageId),
                                    replyMarkup = TgForceReply(
                                        true,
                                        config.user.lang.inputField.enterNickname.ifEmpty { null }
                                    )
                                )
                            } else {
                                newMessage = bot.sendMessage(
                                    chatId = msg.chat.id,
                                    text = BotLogic.escapePlaceholders(config.user.lang.creating.needRequestText, msg.text),
                                    replyParameters = TgReplyParameters(msg.messageId),
                                    replyMarkup = TgForceReply(
                                        true,
                                        config.user.lang.inputField.enterRequestText.ifEmpty { null }
                                    )
                                )
                                it.request_nickname = msg.text
                            }
                            it.message_id_in_chat_with_user = newMessage.messageId.toLong()
                        }
                        else {
                            newMessage = bot.sendMessage(
                                chatId = msg.chat.id,
                                text = BotLogic.escapePlaceholders(config.user.lang.creating.confirmSendRequest, it.request_nickname),
                                replyParameters = TgReplyParameters(msg.messageId),
                                replyMarkup = TgInlineKeyboardMarkup(
                                    listOf(
                                        listOf(
                                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                                text = config.user.lang.button.confirmSending,
                                                callback_data = "send_request"
                                            ),
                                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                                text = config.user.lang.button.cancelRequest,
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
                    RequestType.PENDING -> {
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
                            chatId = config.target.chatId,
                            messageThreadId = config.target.topicId,
                            fromChatId = msg.chat.id,
                            messageId = msg.messageId,
                        )
                        entity.addToTempArray(forwardedMessage.messageId.toString())
                    }
                    else -> {}
                }
            }
        }
        else {
            val replied = msg.replyToMessage?:return
            val entity = MySQLIntegration.getLinkedEntityByTempArrayMessagesId(replied.messageId.toLong())?:return
            if (!entity.tempArray!!.contains(replied.messageId.toString()) || !entity.data!!.requests.any {RequestType.getAllPending().contains(it.request_status)}) return
            bot.forwardMessage(
                chatId = entity.userId,
                fromChatId = msg.chat.id,
                messageId = msg.messageId,
            )
            entity.addToTempArray(msg.messageId.toString())
        }
    }
    suspend fun onTelegramCallbackQuery(cbq: TgCallbackQuery) {
        val entity = MySQLIntegration.getLinkedEntity(cbq.from.id)?:return
        if (entity.isRestricted) return
        when (cbq.data) {
            "agree_with_rules" -> {
                entity.agreedWithRules = true
                val requests = entity.data?.requests?:return
                if (requests.any {it.request_status == RequestType.CREATING}) {
                    val editedRequest = requests.first{it.request_status == RequestType.CREATING}
                    val newMessage = bot.sendMessage(
                        chatId = cbq.from.id,
                        text = BotLogic.escapePlaceholders(config.user.lang.creating.needNickname),
                        replyMarkup = TgForceReply(
                            true,
                            config.user.lang.inputField.enterNickname.ifEmpty { null }
                        )
                    )
                    editedRequest.message_id_in_chat_with_user = newMessage.messageId.toLong()
                    entity.editRequest(editedRequest)
                }
            }
            "redraw_request" -> {
                entity.data = entity.data.let { it!!.requests = ArrayList(it.requests.filter { it1 -> it1.request_status != RequestType.CREATING }); it }
                newRequest(entity)
            }
            "cancel_request" -> cancelRequest(entity)
            "cancel_sending_request" -> cancelSendingRequest(entity)
            "create_request" -> newRequest(entity)
            "send_request" -> {
                val request = entity.data!!.requests.first {it.request_status == RequestType.CREATING}
                bot.forwardMessage(
                    chatId = config.forModerator.chatId,
                    messageThreadId = config.forModerator.topicId,
                    fromChatId = entity.userId,
                    messageId = request.message_id_in_chat_with_user.toInt()
                )
                val messageInChatWithUser = bot.sendMessage(
                    chatId = entity.userId,
                    text = BotLogic.escapePlaceholders(config.user.lang.event.onSend, request.request_nickname),
                    replyParameters = TgReplyParameters(cbq.message.messageId),
                    replyMarkup = TgInlineKeyboardMarkup(listOf(listOf(
                        TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.user.lang.button.cancelRequest,
                            callback_data = "cancel_request"
                        )
                    )))
                )
                val moderatorsControl = bot.sendMessage(
                    chatId = config.forModerator.chatId,
                    messageThreadId = config.forModerator.topicId,
                    text = BotLogic.escapePlaceholders(config.forModerator.lang.event.onNew, request.request_nickname),
                    replyMarkup = TgInlineKeyboardMarkup(listOf(
                        listOf(
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(config.forModerator.lang.button.approveSending, callback_data = "approve_request"),
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(config.forModerator.lang.button.denySending, callback_data = "deny_request"),
                        ),
                        listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(config.forModerator.lang.button.restrictSender, callback_data = "restrict_user")),
                    )),
                    protectContent = true,
                )
                request.request_message_id_in_chat_with_user = request.message_id_in_chat_with_user
                request.message_id_in_chat_with_user = messageInChatWithUser.messageId.toLong()
                request.message_id_in_moderators_chat = moderatorsControl.messageId.toLong()
                request.request_status = RequestType.MODERATING
                entity.editRequest(request)
                MySQLIntegration.setNickname(entity.userId, request.request_nickname!!)
            }
            "approve_request" -> {
                val userEntity = MySQLIntegration.linkedEntities.values.first {
                    it.data!!.requests.any { it1 -> it1.request_status == RequestType.MODERATING && it1.message_id_in_moderators_chat?.toInt() == cbq.message.messageId }
                }
                val request = userEntity.data!!.requests.first { it.request_status == RequestType.MODERATING }
                val forwardedMessage = bot.forwardMessage(
                    chatId = config.target.chatId,
                    messageThreadId = config.target.topicId,
                    fromChatId = userEntity.userId,
                    messageId = request.request_message_id_in_chat_with_user!!.toInt()
                )
                val newMessage = bot.sendMessage(
                    chatId = config.target.chatId,
                    text = BotLogic.escapePlaceholders(config.target.lang.event.onSend, request.request_nickname),
                    replyParameters = TgReplyParameters(forwardedMessage.messageId),
                )
                val poll = bot.sendPoll(
                    chatId = config.target.chatId,
                    messageThreadId = config.target.topicId,
                    question = BotLogic.escapePlaceholders(config.target.lang.poll.question, request.request_nickname),
                    options = listOf(
                        TgInputPollOption(config.target.lang.poll.answerTrue),
                        TgInputPollOption(config.target.lang.poll.answerNull),
                        TgInputPollOption(config.target.lang.poll.answerFalse),
                    ),
                    replyParameters = TgReplyParameters(
                        message_id = forwardedMessage.messageId,
                    ),
                )
                userEntity.addToTempArray(poll.messageId.toString())
                bot.pinMessage(config.target.chatId, forwardedMessage.messageId.toLong(), true)
                bot.editMessageReplyMarkup(
                    chatId = userEntity.userId,
                    messageId = request.message_id_in_chat_with_user.toInt(),
                    replyMarkup = TgReplyMarkup()
                )
                val messageInChatWithUser = bot.sendMessage(
                    chatId = userEntity.userId,
                    text = BotLogic.escapePlaceholders(config.user.lang.event.onApprove, request.request_nickname),
                    replyParameters = TgReplyParameters(request.message_id_in_chat_with_user.toInt()),
                    replyMarkup = TgInlineKeyboardMarkup(listOf(listOf(
                        TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.user.lang.button.cancelRequest,
                            callback_data = "cancel_request"
                        )
                    )))
                )
                val moderatorsControl = bot.editMessageText(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    text = BotLogic.escapePlaceholders(config.forModerator.lang.event.onApprove, request.request_nickname),
                )
                bot.editMessageReplyMarkup(
                    chatId = moderatorsControl.chat.id,
                    messageId = moderatorsControl.messageId,
                    replyMarkup = TgInlineKeyboardMarkup(listOf(
                        listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(config.forModerator.lang.button.closeRequestVote, callback_data = "close_poll")),
                        listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(config.forModerator.lang.button.restrictSender, callback_data = "restrict_user")),
                    ))
                )
                request.message_id_in_chat_with_user = messageInChatWithUser.messageId.toLong()
                request.message_id_in_target_chat = forwardedMessage.messageId.toLong()
                request.poll_message_id = poll.messageId.toLong()
                userEntity.addToTempArray(forwardedMessage.messageId.toString())
                userEntity.addToTempArray(newMessage.messageId.toString())
                request.request_status = RequestType.PENDING
                userEntity.editRequest(request)
            }
            "deny_request" -> {
                val userEntity = MySQLIntegration.linkedEntities.values.first {
                    it.data!!.requests.any { it1 -> it1.request_status == RequestType.MODERATING && it1.message_id_in_moderators_chat?.toInt() == cbq.message.messageId }
                }
                val request = userEntity.data!!.requests.first { it.request_status == RequestType.MODERATING }
                bot.editMessageReplyMarkup(
                    chatId = userEntity.userId,
                    messageId = request.message_id_in_chat_with_user.toInt(),
                    replyMarkup = TgReplyMarkup()
                )
                val messageInChatWithUser = bot.sendMessage(
                    chatId = userEntity.userId,
                    text = BotLogic.escapePlaceholders(config.user.lang.event.onDeny, request.request_nickname),
                    replyParameters = TgReplyParameters(request.message_id_in_chat_with_user.toInt()),
                    replyMarkup = TgInlineKeyboardMarkup(
                        inline_keyboard = listOf(listOf(
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                text = config.user.lang.button.redrawRequest,
                                callback_data = "redraw_request",
                            )))
                    )
                )
                bot.editMessageText(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    text = BotLogic.escapePlaceholders(config.forModerator.lang.event.onDeny, request.request_nickname),
                )
                request.message_id_in_chat_with_user = messageInChatWithUser.messageId.toLong()
                request.request_status = RequestType.DENIED
                userEntity.editRequest(request)
            }
            "restrict_user" -> {
                val userEntity = MySQLIntegration.linkedEntities.values.first {
                    it.data!!.requests.any { it1 -> it1.request_status == RequestType.MODERATING && it1.message_id_in_moderators_chat?.toInt() == cbq.message.messageId }
                }
                val request = userEntity.data!!.requests.first { it.request_status == RequestType.MODERATING }
                bot.editMessageText(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    text = BotLogic.escapePlaceholders(config.user.lang.event.onRestrict, request.request_nickname)
                )
                try {
                    val text4User = config.user.lang.event.onRestrict
                    if (text4User.isNotEmpty()) {
                        bot.sendMessage(
                            chatId = userEntity.userId,
                            text = BotLogic.escapePlaceholders(text4User, entity.nickname ?: entity.userId.toString()),
                        )
                    }
                } catch (_: Exception) {}
                BotLogic.deleteAllProtected(userEntity.data?.protectedMessages?:listOf(), AccountType.UNKNOWN)
                request.request_status = RequestType.DENIED
                userEntity.editRequest(request)
                userEntity.isRestricted = true
            }
            "close_poll" -> {
                if (entity.accountType != AccountType.ADMIN) return
                val userEntity = MySQLIntegration.linkedEntities.values.first {
                    it.data!!.requests.any { it1 -> it1.request_status == RequestType.PENDING && it1.message_id_in_moderators_chat?.toInt() == cbq.message.messageId }
                }
                val request = userEntity.data!!.requests.first { it.request_status == RequestType.PENDING }
                val isAccepted = bot.stopPoll(
                    chatId = config.target.chatId,
                    messageId = request.poll_message_id?.toInt()?:return
                ).options
                    ?.filter { it.text != config.target.lang.poll.answerNull }
                    ?.maxBy { it.voter_count } ?.text?.equals(config.target.lang.poll.answerTrue) ?: false
                RequestsLogic.executeRequestFinalAction(userEntity, isAccepted)
                bot.editMessageText(
                    chatId = config.forModerator.chatId,
                    messageId = request.message_id_in_moderators_chat!!.toInt(),
                    text = BotLogic.escapePlaceholders(config.forModerator.lang.event.onVoteClosed, request.request_nickname),
                )
            }
        }
        if (cbq.message.chat.id > 0) bot.editMessageReplyMarkup(
            chatId = cbq.message.chat.id,
            messageId = cbq.message.messageId,
            replyMarkup = TgReplyMarkup()
        )
    }

    suspend fun onTelegramChatJoinRequest(request: TgChatJoinRequest) {
        val entity = MySQLIntegration.getLinkedEntity(request.from.id)?:return
        if (entity.isRestricted) return
        if (entity.accountType.isPlayer()) {
            bot.approveChatJoinRequest(request.chat.id, request.from.id)
        }
    }
}