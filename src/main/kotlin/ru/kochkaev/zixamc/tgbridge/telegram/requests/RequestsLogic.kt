package ru.kochkaev.zixamc.tgbridge.telegram.requests

import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot.config
import ru.kochkaev.zixamc.tgbridge.telegram.BotLogic
import ru.kochkaev.zixamc.tgbridge.telegram.model.*
import ru.kochkaev.zixamc.tgbridge.sql.SQLChat
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgMenu
import ru.kochkaev.zixamc.tgbridge.sql.data.*
import ru.kochkaev.zixamc.tgbridge.telegram.requests.RequestsBotUpdateManager.Operations
import ru.kochkaev.zixamc.tgbridge.telegram.requests.RequestsBotUpdateManager.RequestCallback

object RequestsLogic {

    suspend fun cancelRequest(entity: SQLEntity): Boolean {
        val request = (entity.data).requests.firstOrNull { RequestType.getAllPending().contains(it.request_status) } ?: return false
        entity.editRequest(request.apply { this.request_status = RequestType.CANCELED })
        bot.sendMessage(
            chatId = entity.userId,
            text = BotLogic.escapePlaceholders(config.user.lang.event.onCanceled, entity.nickname),
            replyMarkup = TgMenu(listOf(listOf(
                SQLCallback.of(
                    display = config.user.lang.button.createRequest,
                    type = "requests",
                    data = RequestCallback(Operations.CREATE_REQUEST),
                )
            )))
        )
        if (request.message_id_in_target_chat != null) bot.sendMessage(
            chatId = config.target.chatId,
            messageThreadId = config.target.topicId,
            text = BotLogic.escapePlaceholders(config.target.lang.event.onCanceled, entity.nickname),
            replyParameters = TgReplyParameters(
                message_id = request.message_id_in_target_chat!!.toInt()
            )
        )
        if (request.poll_message_id != null) bot.stopPoll(
            chatId = config.target.chatId,
            messageId = request.poll_message_id!!.toInt()
        )
        if (request.message_id_in_moderators_chat != null) {
            bot.editMessageReplyMarkup(
                chatId = config.forModerator.chatId,
                messageId = request.message_id_in_moderators_chat!!.toInt(),
                replyMarkup = TgReplyMarkup()
            )
            bot.editMessageText(
                chatId = config.forModerator.chatId,
                messageId = request.message_id_in_moderators_chat!!.toInt(),
                text = BotLogic.escapePlaceholders(config.forModerator.lang.event.onCancel, request.request_nickname)
            )
            SQLCallback.getAll(config.forModerator.chatId, request.message_id_in_moderators_chat!!.toInt()).forEach { it.drop() }
        }
        entity.tempArray.set(listOf())
        return true
    }
    suspend fun cancelSendingRequest(entity: SQLEntity): Boolean {
        entity.data = entity.data.apply {
            this.requests.filter { it.request_status == RequestType.CREATING }
        }
        bot.sendMessage(
            chatId = entity.userId,
            text = config.user.lang.event.onCanceled,
            replyMarkup = TgMenu(listOf(listOf(
                SQLCallback.of(
                    display = BotLogic.escapePlaceholders(config.user.lang.button.createRequest),
                    type = "requests",
                    data = RequestCallback(Operations.CREATE_REQUEST),
                )
            )))
        )
        return true
    }
    suspend fun newRequest(entity: SQLEntity): Boolean {
        when (entity.data.requests.firstOrNull { RequestType.getAllPendingAndCreating().contains(it.request_status) }?.request_status) {
            RequestType.CREATING -> {
                bot.sendMessage(
                    chatId = entity.userId,
                    text = BotLogic.escapePlaceholders(config.user.lang.creating.youAreNowCreatingRequest),
                    replyMarkup = TgMenu(listOf(listOf(
                        SQLCallback.of(
                            display = config.user.lang.button.redrawRequest,
                            type = "requests",
                            data = RequestCallback(Operations.REDRAW_REQUEST),
                        )
                    )))
                )
                return false
            }
            RequestType.MODERATING, RequestType.PENDING -> {
                bot.sendMessage(
                    chatId = entity.userId,
                    text = BotLogic.escapePlaceholders(config.user.lang.creating.youHavePendingRequest, entity.nickname),
                    replyMarkup = TgMenu(listOf(listOf(
                        SQLCallback.of(
                            display = config.user.lang.button.cancelRequest,
                            type = "requests",
                            data = RequestCallback(Operations.CANCEL_REQUEST),
                        )
                    )))
                )
                return false
            }
            else -> {}
        }
        if (entity.accountType.isPlayer) {
            bot.sendMessage(
                chatId = entity.userId,
                text = BotLogic.escapePlaceholders(config.user.lang.creating.youAreNowPlayer, entity.nickname),
            )
            return false
        }
        val forReplyMessage = if (entity.agreedWithRules) bot.sendMessage(
            chatId = entity.userId,
            text = config.user.lang.creating.needNickname,
            replyMarkup = TgForceReply(
                true,
                config.user.lang.inputField.enterNickname.ifEmpty { null }
            )
        )
        else bot.sendMessage(
            chatId = entity.userId,
            text = BotLogic.escapePlaceholders(config.user.lang.creating.needAgreeWithRules),
            replyMarkup = TgMenu(listOf(listOf(
                SQLCallback.of(
                    display = config.user.lang.button.agreeWithRules,
                    type = "requests",
                    data = RequestCallback(Operations.AGREE_WITH_RULES),
                )
            )))
        )
        entity.addRequest(
            RequestData(
            (entity.data.requests.maxOfOrNull { it.user_request_id } ?: -1)+1,
            null,
            forReplyMessage.messageId.toLong(),
            null,
            null,
            null,
            RequestType.CREATING,
            null,
        ))
        if (entity.accountType == AccountType.UNKNOWN) entity.accountType = AccountType.REQUESTER
        return true
    }

    fun promoteUser(argEntity: SQLEntity? = null, userId: Long? = null, nickname: String? = null, targetName: String? = null, argTargetId: Int? = null, argTarget: AccountType? = null): Boolean {
        val entity = argEntity ?:
            if (userId != null) SQLEntity.get(userId) ?: return false
            else if (nickname != null) SQLEntity.get(nickname) ?: return false
            else return false
        val target = argTarget ?:
            if (argTargetId!=null) AccountType.parse(argTargetId)
            else if (targetName!=null) AccountType.parse(targetName)
            else AccountType.UNKNOWN
        entity.accountType = target
        return entity.accountType==target
    }

    fun checkPermissionToExecute(
        message: TgMessage?,
        entity: SQLEntity = SQLEntity.getOrCreate(message?.from!!.id),
        allowedAccountTypes: List<AccountType> = listOf(AccountType.ADMIN),
        allowedIfSpendByItself: Boolean = false,
    ): Boolean =
        (entity.accountType in allowedAccountTypes || (allowedIfSpendByItself && message?.from?.id==entity.userId))

    fun matchEntityFromUpdateServerPlayerStatusCommand(msg: TgMessage?, allowedIfSpendByItself: Boolean = false): SQLEntity? {
        if (msg == null) return null
        val args = msg.text!!.split(" ")
        val isArgUserId = if (args.size > 1) args[1].matches("[0-9]+".toRegex()) && args[1].length == 10 else false
        val isReplyToMessage = msg.replyToMessage != null
        val isItLegalReply = isReplyToMessage && msg.replyToMessage!!.messageId != config.target.topicId
        val entity =
            if (isArgUserId)
                SQLEntity.get(args[1].toLong())
            else if (isItLegalReply)
                SQLEntity.get(msg.replyToMessage!!.from?.id ?: return null)
            else if (!isReplyToMessage && args.size>1 && args[1].matches("[a-zA-Z0-9_]+".toRegex()) && args[1].length in 3..16)
                SQLEntity.get(args[1])
            else if (allowedIfSpendByItself)
                SQLEntity.get(msg.from!!.id)
            else null
        return entity
    }

    fun updateServerPlayerStatus(
        entity: SQLEntity,
        applyAccountStatuses: List<MinecraftAccountType> = MinecraftAccountType.getAll(),
        targetAccountStatus: MinecraftAccountType = MinecraftAccountType.PLAYER,
        targetAccountType: AccountType = targetAccountStatus.toAccountType(),
        editWhitelist: Boolean = false
    ) : Boolean {
        val isTargetPlayer = targetAccountType.isPlayer
        if (!promoteUser(
                argEntity = entity,
                argTarget = targetAccountType,
            )
        ) return false
        else {
            entity.data.minecraftAccounts
                .filter { applyAccountStatuses.contains(it.accountStatus) }
                .map { it.nickname }
                .forEach {
                    if (editWhitelist) {
                        if (isTargetPlayer) WhitelistManager.add(it)
                        else WhitelistManager.remove(it)
                    }
                    entity.editMinecraftAccount(it, targetAccountStatus)
                }
            return true
        }
    }

    suspend fun sendOnJoinInfoMessage(
        chat: SQLChat,
        replyToMessageID: Int? = null,
    ) : TgMessage = BotLogic.sendInfoMessage(
            bot = bot,
            chat = chat,
            replyParameters = if (replyToMessageID!=null) TgReplyParameters(
                replyToMessageID
            ) else null,
            replyMarkup = TgInlineKeyboardMarkup(
                listOf(
                    listOf(
                        TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.user.lang.button.joinToPlayersGroup,
                            url = config.playersGroupInviteLink,
                        )
                    ),
                    listOf(BotLogic.copyIPReplyMarkup),
                )
            ),
        )

    suspend fun executeCheckPermissionsAndExceptions(
        message: TgMessage?,
        entity: SQLEntity?,
        entityExecutor: SQLEntity? = if (message!=null) SQLEntity.get(message.from!!.id) else null,
        allowedExecutionAccountTypes: List<AccountType> = listOf(AccountType.ADMIN),
        allowedExecutionIfSpendByItself: Boolean = false,
        applyAccountStatuses: List<MinecraftAccountType> = MinecraftAccountType.getAll(),
        targetAccountStatus: MinecraftAccountType = MinecraftAccountType.PLAYER,
        targetAccountType: AccountType = targetAccountStatus.toAccountType(),
        editWhitelist: Boolean = false,
        helpText: String? = null,
    ) : Boolean {
        var errorDueExecuting = false
        var havePermission = true
        if (entityExecutor == null || entity == null) errorDueExecuting = true
        else havePermission = checkPermissionToExecute(
            message = message,
            entity = entityExecutor,
            allowedAccountTypes = allowedExecutionAccountTypes,
            allowedIfSpendByItself = allowedExecutionIfSpendByItself,
        )
        if (!havePermission) errorDueExecuting = true
        if (!errorDueExecuting && !updateServerPlayerStatus(
                entity = entity!!,
                applyAccountStatuses = applyAccountStatuses,
                targetAccountStatus = targetAccountStatus,
                targetAccountType = targetAccountType,
                editWhitelist = editWhitelist,
            )
        ) errorDueExecuting = true
        if (errorDueExecuting && message!=null && helpText != null) {
            bot.sendMessage(
                chatId = message.chat.id,
                text =
                    if (!havePermission) BotLogic.escapePlaceholders(
                        config.commonLang.command.permissionDenied,
                        entity?.nickname
                    )
                    else BotLogic.escapePlaceholders(helpText, entity?.nickname),
                replyParameters = TgReplyParameters(message.messageId),
            )
        }
        return errorDueExecuting
    }
    suspend fun executeRequestFinalAction(
        entity: SQLEntity,
        isAccepted: Boolean,
    ) : Boolean {
        val request = entity.data.requests.firstOrNull {it.request_status == RequestType.PENDING} ?: return false
        val message4User = BotLogic.escapePlaceholders(
            text = if (isAccepted) config.user.lang.event.onAccept else config.user.lang.event.onReject,
            nickname = request.request_nickname,
        )
        val message4Target = BotLogic.escapePlaceholders(
            text = if (isAccepted) config.target.lang.event.onAccept else config.target.lang.event.onReject,
            nickname = request.request_nickname,
        )
        bot.sendMessage(
            chatId = config.target.chatId,
            text = message4Target,
            replyParameters = TgReplyParameters(request.poll_message_id!!.toInt()),
        )
        val newMessage = bot.sendMessage(
            chatId = entity.userId,
            text = message4User,
            replyParameters = TgReplyParameters(request.message_id_in_chat_with_user.toInt()),
            protectContent = false,
        )
        bot.editMessageReplyMarkup(
            chatId = entity.userId,
            messageId = request.message_id_in_chat_with_user.toInt(),
            replyMarkup = TgReplyMarkup()
        )
        request.request_status = if (isAccepted) RequestType.ACCEPTED else RequestType.REJECTED
        request.message_id_in_chat_with_user = newMessage.messageId.toLong()
        entity.editRequest(request)
        entity.tempArray.set(listOf())
        if (isAccepted) {
            sendOnJoinInfoMessage(entity, newMessage.messageId)
            entity.accountType = AccountType.PLAYER
            entity.addMinecraftAccount(MinecraftAccountData(request.request_nickname!!, MinecraftAccountType.PLAYER))
            try { WhitelistManager.add(request.request_nickname!!) } catch (_:Exception) {}
        }
        return true
    }
    suspend fun updateRules(
        entity: SQLEntity,
        toReplyMessageId: Int? = null,
        revokeAccepts: Boolean = false,
    ): Boolean {
        if (!checkPermissionToExecute(
                null, entity, listOf(AccountType.ADMIN), false
            )) return true
//        bot.sendMessage(
//            chatId = config.target.chatId,
//            text = BotLogic.escapePlaceholders(config.target.lang.event.onRulesUpdated),
//            replyMarkup = TgMenu(listOf(listOf(
//                if (revokeAccepts)
//                    SQLCallback.of(
//                        display = config.user.lang.button.agreeWithRules,
//                        type = "requests",
//                        data = RequestCallback(Operations.AGREE_WITH_RULES),
//                    )
//                else
//                    SQLCallback.of(
//                        display = config.user.lang.button.revokeAgreeWithRules,
//                        type = "requests",
//                        data = RequestCallback(Operations.REVOKE_AGREE_WITH_RULES),
//                    )
//            ))),
//            replyParameters = if (toReplyMessageId!=null) TgReplyParameters(
//                toReplyMessageId
//            ) else null,
//        )
        SQLGroup.groups.forEach { it.getSQLAssert().sendRulesUpdated(revokeAccepts) }
        SQLEntity.users.map {it.getSQLAssert()} .filter { it.agreedWithRules } .forEach {
            if (revokeAccepts) it.agreedWithRules = false
            try {
                bot.sendMessage(
                    chatId = it.userId,
                    text = BotLogic.escapePlaceholders(config.user.lang.event.onRulesUpdated),
                    replyMarkup = TgMenu(listOf(listOf(
                        if (revokeAccepts)
                            SQLCallback.of(
                                display = config.user.lang.button.agreeWithRules,
                                type = "requests",
                                data = RequestCallback(Operations.AGREE_WITH_RULES),
                            )
                        else
                            SQLCallback.of(
                                display = config.user.lang.button.revokeAgreeWithRules,
                                type = "requests",
                                data = RequestCallback(Operations.REVOKE_AGREE_WITH_RULES),
                            )
                    ))),
                )
            } catch (_: Exception) {}
        }
        return true
    }
}