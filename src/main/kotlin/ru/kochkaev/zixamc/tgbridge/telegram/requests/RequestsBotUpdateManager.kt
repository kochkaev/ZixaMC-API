package ru.kochkaev.zixamc.tgbridge.telegram.requests

import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.telegram.BotLogic
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot.config
import ru.kochkaev.zixamc.tgbridge.telegram.requests.RequestsLogic.cancelRequest
import ru.kochkaev.zixamc.tgbridge.telegram.requests.RequestsLogic.cancelSendingRequest
import ru.kochkaev.zixamc.tgbridge.telegram.requests.RequestsLogic.newRequest
import ru.kochkaev.zixamc.tgbridge.telegram.model.*
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.sql.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgMenu
import ru.kochkaev.zixamc.tgbridge.sql.data.AccountType
import ru.kochkaev.zixamc.tgbridge.sql.data.MinecraftAccountType
import ru.kochkaev.zixamc.tgbridge.sql.data.RequestType
import ru.kochkaev.zixamc.tgbridge.sql.util.*
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureTypes
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser

object RequestsBotUpdateManager {
    suspend fun onTelegramMessage(msg: TgMessage) {
        if (msg.chat.id>=0) {
            val entity = SQLEntity.get(msg.from!!.id)?:return
            if (entity.isRestricted) return
            if (entity.accountType == AccountType.REQUESTER) {
                val requesterData = entity.data
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
                                    replyParameters = TgReplyParameters(
                                        msg.messageId
                                    ),
                                    replyMarkup = TgForceReply(
                                        true,
                                        config.user.lang.inputField.enterNickname.ifEmpty { null }
                                    )
                                )
                            } else if (!entity.canTakeNickname(msg.text)) {
                                newMessage = bot.sendMessage(
                                    chatId = msg.chat.id,
                                    text = BotLogic.escapePlaceholders(config.user.lang.creating.takenNickname, msg.text),
                                    replyParameters = TgReplyParameters(
                                        msg.messageId
                                    ),
                                    replyMarkup = TgForceReply(
                                        true,
                                        config.user.lang.inputField.enterNickname.ifEmpty { null }
                                    )
                                )
                            } else {
                                newMessage = bot.sendMessage(
                                    chatId = msg.chat.id,
                                    text = BotLogic.escapePlaceholders(config.user.lang.creating.needRequestText, msg.text),
                                    replyParameters = TgReplyParameters(
                                        msg.messageId
                                    ),
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
                                replyParameters = TgReplyParameters(
                                    msg.messageId
                                ),
                                replyMarkup = TgMenu(listOf(listOf(
                                    SQLCallback.of(
                                        display = config.user.lang.button.confirmSending,
                                        type = "requests",
                                        data = RequestCallback(Operations.SEND_REQUEST),
                                    ),
                                    SQLCallback.of(
                                        display = config.user.lang.button.cancelRequest,
                                        type = "requests",
                                        data = RequestCallback(Operations.CANCEL_SENDING_REQUEST),
                                    ),
                                )))
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
                        entity.tempArray.add(forwardedMessage.messageId.toString())
                    }
                    else -> {}
                }
            }
        }
        else {
            val replied = msg.replyToMessage?:return
            val entity = SQLEntity.getByTempArray(replied.messageId.toString())?:return
            if (!entity.tempArray.contains(replied.messageId.toString()) || !entity.data.requests.any { RequestType.getAllPending().contains(it.request_status)}) return
            bot.forwardMessage(
                chatId = entity.userId,
                fromChatId = msg.chat.id,
                messageId = msg.messageId,
            )
            entity.tempArray.add(msg.messageId.toString())
        }
    }
    suspend fun onTelegramCallbackQuery(cbq: TgCallbackQuery, sql: SQLCallback<RequestCallback>): TgCBHandlerResult {
        val entity = SQLEntity.get(cbq.from.id)?:return TgCBHandlerResult.SUCCESS
        if (entity.isRestricted) return TgCBHandlerResult.DELETE_MARKUP
        when (sql.data?.operation) {
            Operations.AGREE_WITH_RULES -> {
                entity.agreedWithRules = true
                val requests = entity.data.requests
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
            Operations.REVOKE_AGREE_WITH_RULES -> {
                bot.sendMessage(
                    chatId = cbq.message.chat.id,
                    text = TextParser.formatLang(config.commonLang.areYouSureRevokeAgreeWithRules, "nickname" to (entity.nickname?:cbq.from.firstName)),
                    replyMarkup = TgMenu(listOf(
                        listOf(SQLCallback.of(
                            display = ServerBot.config.integration.group.confirm,
                            type = "requests",
                            data = RequestCallback(Operations.CONFIRM_REVOKE_AGREE_WITH_RULES, entity.userId)
                        )),
                        listOf(SQLCallback.of(
                            display = ServerBot.config.integration.group.cancelConfirm,
                            type = "requests",
                            data = RequestCallback(Operations.SUCCESS, entity.userId)
                        )),
                    ))
                )
                return TgCBHandlerResult.SUCCESS
            }
            Operations.CONFIRM_REVOKE_AGREE_WITH_RULES -> {
                if (sql.data?.userId != entity.userId) {
                    bot.answerCallbackQuery(
                        callbackQueryId = cbq.id,
                        text = TextParser.formatLang(
                            text = config.commonLang.thatButtonFor,
                            "nickname" to (sql.data?.userId?.let { SQLEntity.get(it)?.nickname ?: it.toString() } ?:"")
                        ),
                        showAlert = true,
                    )
                    return TgCBHandlerResult.SUCCESS
                }
                entity.agreedWithRules = false
                RequestsCommandLogic.executeUpdateServerPlayerStatusCommand(
                    message = null,
                    allowedExecutionAccountTypes = AccountType.entries,
                    allowedExecutionIfSpendByItself = true,
                    applyAccountStatuses = MinecraftAccountType.getAllActiveNow(),
                    targetAccountStatus = MinecraftAccountType.FROZEN,
                    editWhitelist = true,
                    helpText = null,
                    text4User = config.user.lang.event.onLeave,
                    text4Target = config.target.lang.event.onLeave,
                    removePreviousTgReplyMarkup = true,
                    removeProtectedContent = true,
                    entity = entity,
                    entityExecutor = entity,
                    messageForReplyId = cbq.message.messageId,
                )
                return TgCBHandlerResult.DELETE_MARKUP
            }
            Operations.REDRAW_REQUEST -> {
                entity.data = entity.data.let { it.requests = ArrayList(it.requests.filter { it1 -> it1.request_status != RequestType.CREATING }); it }
                newRequest(entity)
            }
            Operations.CANCEL_REQUEST -> cancelRequest(entity)
            Operations.CANCEL_SENDING_REQUEST -> cancelSendingRequest(entity)
            Operations.CREATE_REQUEST -> newRequest(entity)
            Operations.SEND_REQUEST -> {
                val request = entity.data.requests.first {it.request_status == RequestType.CREATING}
                val forwarded = bot.forwardMessage(
                    chatId = config.forModerator.chatId,
                    messageThreadId = config.forModerator.topicId,
                    fromChatId = entity.userId,
                    messageId = request.message_id_in_chat_with_user.toInt()
                )
                val messageInChatWithUser = bot.sendMessage(
                    chatId = entity.userId,
                    text = BotLogic.escapePlaceholders(config.user.lang.event.onSend, request.request_nickname),
                    replyParameters = TgReplyParameters(cbq.message.messageId),
                    replyMarkup = TgMenu(listOf(listOf(
                        SQLCallback.of(
                            display = config.user.lang.button.cancelRequest,
                            type = "requests",
                            data = RequestCallback(Operations.CANCEL_REQUEST),
                        )
                    )))
                )
                val moderatorsControl = bot.sendMessage(
                    chatId = config.forModerator.chatId,
                    messageThreadId = config.forModerator.topicId,
                    text = BotLogic.escapePlaceholders(config.forModerator.lang.event.onNew, request.request_nickname),
                    replyMarkup = TgMenu(listOf(
                        listOf(
                            SQLCallback.of(
                                display = config.forModerator.lang.button.approveSending,
                                type = "requests",
                                data = RequestCallback(Operations.APPROVE_REQUEST),
                            ),
                            SQLCallback.of(
                                display = config.forModerator.lang.button.denySending,
                                type = "requests",
                                data = RequestCallback(Operations.DENY_REQUEST),
                            ),
                        ),
                        listOf(SQLCallback.of(
                            display = config.forModerator.lang.button.restrictSender,
                            type = "requests",
                            data = RequestCallback(Operations.RESTRICT_USER),
                        )),
                    )),
                    replyParameters = TgReplyParameters(forwarded.messageId),
                    protectContent = true,
                )
                request.request_message_id_in_chat_with_user = request.message_id_in_chat_with_user
                request.message_id_in_chat_with_user = messageInChatWithUser.messageId.toLong()
                request.message_id_in_moderators_chat = moderatorsControl.messageId.toLong()
                request.request_status = RequestType.MODERATING
                entity.editRequest(request)
                entity.addNickname(request.request_nickname!!)
            }
            Operations.APPROVE_REQUEST -> {
                val userEntity = SQLEntity.users.map(LinkedUser::getSQLAssert).first {
                    it.data.requests.any { it1 -> it1.request_status == RequestType.MODERATING && it1.message_id_in_moderators_chat?.toInt() == cbq.message.messageId }
                }
                val request = userEntity.data.requests.first { it.request_status == RequestType.MODERATING }
                val forwardedMessage = bot.forwardMessage(
                    chatId = config.target.chatId,
                    messageThreadId = config.target.topicId,
                    fromChatId = cbq.message.chat.id,
                    messageId = cbq.message.replyToMessage?.messageId?:return TgCBHandlerResult.SUCCESS
                )
                val newMessage = bot.sendMessage(
                    chatId = config.target.chatId,
                    text = BotLogic.escapePlaceholders(config.target.lang.event.onSend, request.request_nickname),
                    replyParameters = TgReplyParameters(
                        forwardedMessage.messageId
                    ),
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
                userEntity.tempArray.add(poll.messageId.toString())
                bot.pinMessage(config.target.chatId, forwardedMessage.messageId.toLong(), true)
                bot.editMessageReplyMarkup(
                    chatId = userEntity.userId,
                    messageId = request.message_id_in_chat_with_user.toInt(),
                    replyMarkup = TgReplyMarkup()
                )
                SQLCallback.getAll(userEntity.userId, request.message_id_in_chat_with_user.toInt()).forEach { it.drop() }
                val messageInChatWithUser = bot.sendMessage(
                    chatId = userEntity.userId,
                    text = BotLogic.escapePlaceholders(config.user.lang.event.onApprove, request.request_nickname),
                    replyParameters = TgReplyParameters(request.message_id_in_chat_with_user.toInt()),
                    replyMarkup = TgMenu(listOf(listOf(
                        SQLCallback.of(
                            display = config.user.lang.button.cancelRequest,
                            type = "requests",
                            data = RequestCallback(Operations.CANCEL_REQUEST),
                        )
                    )))
                )
                val moderatorsControl = bot.editMessageText(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    text = BotLogic.escapePlaceholders(config.forModerator.lang.event.onApprove, request.request_nickname),
                )
                SQLCallback.getAll(moderatorsControl.chat.id, moderatorsControl.messageId).forEach { it.drop() }
                bot.editMessageReplyMarkup(
                    chatId = moderatorsControl.chat.id,
                    messageId = moderatorsControl.messageId,
                    replyMarkup = TgMenu(listOf(
                        listOf(SQLCallback.of(
                            display = config.forModerator.lang.button.closeRequestVote,
                            type = "requests",
                            data = RequestCallback(Operations.CLOSE_POLL)
                        )),
                        listOf(SQLCallback.of(
                            display = config.forModerator.lang.button.restrictSender,
                            type = "requests",
                            data = RequestCallback(Operations.RESTRICT_USER)
                        )),
                    ))
                )
                request.message_id_in_chat_with_user = messageInChatWithUser.messageId.toLong()
                request.message_id_in_target_chat = forwardedMessage.messageId.toLong()
                request.poll_message_id = poll.messageId.toLong()
                userEntity.tempArray.add(forwardedMessage.messageId.toString())
                userEntity.tempArray.add(newMessage.messageId.toString())
                request.request_status = RequestType.PENDING
                userEntity.editRequest(request)
            }
            Operations.DENY_REQUEST -> {
                val userEntity = SQLEntity.users.map(LinkedUser::getSQLAssert).first {
                    it.data.requests.any { it1 -> it1.request_status == RequestType.MODERATING && it1.message_id_in_moderators_chat?.toInt() == cbq.message.messageId }
                }
                val request = userEntity.data.requests.first { it.request_status == RequestType.MODERATING }
                bot.editMessageReplyMarkup(
                    chatId = userEntity.userId,
                    messageId = request.message_id_in_chat_with_user.toInt(),
                    replyMarkup = TgReplyMarkup()
                )
                SQLCallback.getAll(userEntity.userId, request.message_id_in_chat_with_user.toInt()).forEach { it.drop() }
                val messageInChatWithUser = bot.sendMessage(
                    chatId = userEntity.userId,
                    text = BotLogic.escapePlaceholders(config.user.lang.event.onDeny, request.request_nickname),
                    replyParameters = TgReplyParameters(request.message_id_in_chat_with_user.toInt()),
                    replyMarkup = TgMenu(listOf(listOf(
                        SQLCallback.of(
                            display = config.user.lang.button.redrawRequest,
                            type = "requests",
                            data = RequestCallback(Operations.REDRAW_REQUEST),
                        )
                    )))
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
            Operations.RESTRICT_USER -> {
                val userEntity = SQLEntity.users.map(LinkedUser::getSQLAssert).first {
                    it.data.requests.any { it1 -> it1.request_status == RequestType.MODERATING && it1.message_id_in_moderators_chat?.toInt() == cbq.message.messageId }
                }
                val request = userEntity.data.requests.first { it.request_status == RequestType.MODERATING }
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
                userEntity.deleteProtected(AccountType.UNKNOWN)
                request.request_status = RequestType.DENIED
                userEntity.editRequest(request)
                userEntity.isRestricted = true
            }
            Operations.CLOSE_POLL -> {
                if (entity.accountType != AccountType.ADMIN) return TgCBHandlerResult.SUCCESS
                SQLCallback.getAll(cbq.message.chat.id, cbq.message.messageId).forEach { it.drop() }
                val userEntity = SQLEntity.users.map(LinkedUser::getSQLAssert).first {
                    it.data.requests.any { it1 -> it1.request_status == RequestType.PENDING && it1.message_id_in_moderators_chat?.toInt() == cbq.message.messageId }
                }
                val request = userEntity.data.requests.first { it.request_status == RequestType.PENDING }
                SQLCallback.getAll(userEntity.userId, request.message_id_in_chat_with_user.toInt()).forEach { it.drop() }
                val isAccepted = bot.stopPoll(
                    chatId = config.target.chatId,
                    messageId = request.poll_message_id?.toInt()?:return TgCBHandlerResult.SUCCESS
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
            Operations.SUCCESS -> {
                if (sql.data?.userId != entity.userId) {
                    bot.answerCallbackQuery(
                        callbackQueryId = cbq.id,
                        text = TextParser.formatLang(
                            text = config.commonLang.thatButtonFor,
                            "nickname" to (sql.data?.userId?.let { SQLEntity.get(it)?.nickname ?: it.toString() } ?:"")
                        ),
                        showAlert = true,
                    )
                    return TgCBHandlerResult.SUCCESS
                }
                bot.deleteMessage(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                )
            }
            else -> {}
        }
        return if (cbq.message.chat.id>0) TgCBHandlerResult.DELETE_MARKUP else TgCBHandlerResult.SUCCESS
    }

    suspend fun onTelegramChatJoinRequest(request: TgChatJoinRequest) {
        val group = SQLGroup.get(request.chat.id)?:return
        if (group.features.getCasted(FeatureTypes.PLAYERS_GROUP)?.autoAccept == true) {
            val user = SQLEntity.get(request.from.id)?:return
            if (user.isRestricted) return
            if (user.accountType.isHigherThanOrEqual(AccountType.PLAYER)) {
                try {
                    bot.approveChatJoinRequest(request.chat.id, request.from.id)
                } catch (_: Exception) { try {
                    ServerBot.bot.approveChatJoinRequest(request.chat.id, request.from.id)
                } catch (_: Exception) {} }
                group.members.add(request.from.id)
            }
        }
    }
    open class RequestCallback(
        var operation: Operations,
        var userId: Long? = null,
    ): CallbackData
    enum class Operations {
        @SerializedName("agree_with_rules")
        AGREE_WITH_RULES,
        @SerializedName("revoke_agree_with_rules")
        REVOKE_AGREE_WITH_RULES,
        @SerializedName("confirm_revoke_agree_with_rules")
        CONFIRM_REVOKE_AGREE_WITH_RULES,
        @SerializedName("redraw_request")
        REDRAW_REQUEST,
        @SerializedName("cancel_request")
        CANCEL_REQUEST,
        @SerializedName("cancel_sending_request")
        CANCEL_SENDING_REQUEST,
        @SerializedName("create_request")
        CREATE_REQUEST,
        @SerializedName("send_request")
        SEND_REQUEST,
        @SerializedName("approve_request")
        APPROVE_REQUEST,
        @SerializedName("deny_request")
        DENY_REQUEST,
        @SerializedName("restrict_user")
        RESTRICT_USER,
        @SerializedName("close_poll")
        CLOSE_POLL,
        @SerializedName("success")
        SUCCESS,
    }
}