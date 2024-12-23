package ru.kochkaev.zixamc.tgbridge.requests

import ru.kochkaev.zixamc.tgbridge.BotLogic
import ru.kochkaev.zixamc.tgbridge.MySQLIntegration
import ru.kochkaev.zixamc.tgbridge.RequestsBot.bot
import ru.kochkaev.zixamc.tgbridge.RequestsBot.config
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.cancelRequest
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.cancelSendingRequest
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.newRequest
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.*

object RequestsBotUpdateManager {
    suspend fun onTelegramMessage(msg: TgMessage) {
        if (msg.chat.id>=0) {
            val entity = MySQLIntegration.getLinkedEntity(msg.from!!.id)?:return
            if (entity.isRestricted) return
            if (entity.accountType == 2) {
                val requesterData = entity.data?:return
                if (!entity.agreedWithRules) {
                    bot.sendMessage(
                        msg.chat.id,
                        config.text.messages.textMustAgreeWithRules,
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
                                    text = BotLogic.escapePlaceholders(config.text.messages.textWrongNickname, msg.text),
                                    replyParameters = TgReplyParameters(msg.messageId),
                                    replyMarkup = TgForceReply(
                                        true,
                                        config.text.inputFields.textInputFieldPlaceholderNickname.ifEmpty { null }
                                    )
                                )
                            } else if (MySQLIntegration.isNicknameNotAvailableToTake(entity.userId, msg.text)) {
                                newMessage = bot.sendMessage(
                                    chatId = msg.chat.id,
                                    text = BotLogic.escapePlaceholders(config.text.messages.textTakenNickname, msg.text),
                                    replyParameters = TgReplyParameters(msg.messageId),
                                    replyMarkup = TgForceReply(
                                        true,
                                        config.text.inputFields.textInputFieldPlaceholderNickname.ifEmpty { null }
                                    )
                                )
                            } else {
                                newMessage = bot.sendMessage(
                                    chatId = msg.chat.id,
                                    text = BotLogic.escapePlaceholders(config.text.messages.textOnNewRequest, msg.text),
                                    replyParameters = TgReplyParameters(msg.messageId),
                                    replyMarkup = TgForceReply(
                                        true,
                                        config.text.inputFields.textInputFieldPlaceholderRequest.ifEmpty { null }
                                    )
                                )
                                it.request_nickname = msg.text
                            }
                            it.message_id_in_chat_with_user = newMessage.messageId.toLong()
                        }
                        else {
                            newMessage = bot.sendMessage(
                                chatId = msg.chat.id,
                                text = BotLogic.escapePlaceholders(config.text.messages.textConfirmSendRequest, it.request_nickname),
                                replyParameters = TgReplyParameters(msg.messageId),
                                replyMarkup = TgInlineKeyboardMarkup(
                                    listOf(
                                        listOf(
                                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                                text = config.text.buttons.textButtonConfirmSending,
                                                callback_data = "send_request"
                                            ),
                                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                                text = config.text.buttons.textButtonCancelRequest,
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
            if (!entity.tempArray!!.contains(replied.messageId.toString()) || !entity.data!!.requests.any {it.request_status == "pending"}) return
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
                if (requests.any {it.request_status == "creating"}) {
                    val editedRequest = requests.first{it.request_status == "creating"}
                    val newMessage = bot.sendMessage(
                        chatId = cbq.from.id,
                        text = BotLogic.escapePlaceholders(config.text.messages.textNeedNickname),
                        replyMarkup = TgForceReply(
                            true,
                            config.text.inputFields.textInputFieldPlaceholderNickname.ifEmpty { null }
                        )
                    )
                    editedRequest.message_id_in_chat_with_user = newMessage.messageId.toLong()
                    entity.editRequest(editedRequest)
                }
            }
            "redraw_request" -> {
                entity.data = entity.data.let { it!!.requests = ArrayList(it.requests.filter { it1 -> it1.request_status != "creating" }); it }
                newRequest(entity)
            }
            "cancel_request" -> cancelRequest(entity)
            "cancel_sending_request" -> cancelSendingRequest(entity)
            "create_request" -> newRequest(entity)
            "send_request" -> {
                val request = entity.data!!.requests.first {it.request_status == "creating"}
                val forwardedMessage = bot.forwardMessage(
                    chatId = config.targetChatId,
                    messageThreadId = config.targetTopicId,
                    fromChatId = cbq.from.id,
                    messageId = request.message_id_in_chat_with_user.toInt()
                )
                val newMessage = bot.sendMessage(
                    chatId = config.targetChatId,
                    text = BotLogic.escapePlaceholders(config.text.events.forTarget.textOnSend4Target, request.request_nickname),
                    replyParameters = TgReplyParameters(forwardedMessage.messageId),
                )
                if (config.poll.autoCreatePoll) {
                    val poll = bot.sendPoll(
                        chatId = config.targetChatId,
                        messageThreadId = config.targetTopicId,
                        question = BotLogic.escapePlaceholders(config.poll.pollQuestion, request.request_nickname),
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
                val messageInChatWithUser = bot.sendMessage(
                    chatId = cbq.from.id,
                    text = BotLogic.escapePlaceholders(config.text.events.forUser.textOnSend4User, request.request_nickname),
                    replyParameters = TgReplyParameters(cbq.message.messageId),
                    replyMarkup = TgInlineKeyboardMarkup(listOf(listOf(
                        TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.text.buttons.textButtonCancelRequest,
                            callback_data = "cancel_request"
                        )
                    )))
                )
                request.message_id_in_chat_with_user = messageInChatWithUser.messageId.toLong()
                request.message_id_in_target_chat = forwardedMessage.messageId.toLong()
                entity.addToTempArray(forwardedMessage.messageId.toString())
                entity.addToTempArray(newMessage.messageId.toString())
                request.request_status = "pending"
                entity.editRequest(request)
                MySQLIntegration.setNickname(entity.userId, request.request_nickname!!)
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
        if (entity.accountType<=1) {
            bot.approveChatJoinRequest(request.chat.id, request.from.id)
        }
    }
}