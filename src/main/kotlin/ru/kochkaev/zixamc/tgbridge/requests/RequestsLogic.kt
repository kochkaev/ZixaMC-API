package ru.kochkaev.zixamc.tgbridge.requests

import ru.kochkaev.zixamc.tgbridge.NewMySQLIntegration
import ru.kochkaev.zixamc.tgbridge.NewSQLEntity
import ru.kochkaev.zixamc.tgbridge.RequestsBot.bot
import ru.kochkaev.zixamc.tgbridge.RequestsBot.config
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.RequestData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.*

object RequestsLogic {

    suspend fun cancelRequest(entity: NewSQLEntity): Boolean {
        entity.editRequest((entity.data?:return false).requests.first { it.request_status == "pending" }.apply { this.request_status = "canceled" })
        bot.sendMessage(
            chatId = entity.userId,
            text = config.text.textRequestCanceled4User,
            replyMarkup = TgInlineKeyboardMarkup(
                inline_keyboard = listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
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
        entity.tempArray = arrayOf()
        return true
    }
    suspend fun cancelSendingRequest(entity: NewSQLEntity): Boolean {
        entity.data = entity.data.apply {
            (this ?: return false).requests.filter { it.request_status == "creating" }
        }
        bot.sendMessage(
            chatId = entity.userId,
            text = config.text.textRequestCanceled4User,
            replyMarkup = TgInlineKeyboardMarkup(
                inline_keyboard = listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.textButtonCreateRequest,
                    callback_data = "create_request",
                )))
            )
        )
        return true
    }
    suspend fun newRequest(entity: NewSQLEntity): Boolean {
        when (entity.createAndOrGetData().requests.firstOrNull { listOf("creating", "pending").contains(it.request_status) }?.request_status ?: "") {
            "creating" -> {
                bot.sendMessage(
                    chatId = entity.userId,
                    text = config.text.textYouAreNowCreatingRequest,
                    replyMarkup = TgInlineKeyboardMarkup(
                        inline_keyboard = listOf(listOf(
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.text.textButtonRedrawRequest,
                            callback_data = "redraw_request",
                        )))
                    )
                )
                return false
            }
            "pending" -> {
                bot.sendMessage(
                    chatId = entity.userId,
                    text = config.text.textYouHavePendingRequest,
                    replyMarkup = TgInlineKeyboardMarkup(
                        inline_keyboard = listOf(listOf(
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.text.textButtonCancelRequest,
                            callback_data = "cancel_request",
                        )))
                    )
                )
                return false
            }
        }
        if (entity.accountType<2) {
            bot.sendMessage(
                chatId = entity.userId,
                text = config.text.textYouAreNowPlayer,
            )
            return false
        }
        val forReplyMessage = if (NewMySQLIntegration.isAgreedWithRules(entity.userId)) bot.sendMessage(
            chatId = entity.userId,
            text = config.text.textNeedNickname,
            replyMarkup = TgForceReply(
                true,
                config.text.textInputFieldPlaceholderNickname.ifEmpty { null }
            )
        )
        else bot.sendMessage(
            chatId = entity.userId,
            text = config.text.textNeedAgreeWithRules,
            replyMarkup = TgInlineKeyboardMarkup(
                listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.textButtonAgreeWithRules,
                    callback_data = "agree_with_rules",
                ))),
            )
        )
        NewMySQLIntegration.addRequest(entity.userId, RequestData(
            (entity.data?.requests?.maxOfOrNull { it.user_request_id } ?: -1)+1,
            null,
            forReplyMessage.messageId.toLong(),
            "creating",
            null,
        )
        )
        if (entity.accountType>2) entity.accountType = 2
        return true
    }

    fun promoteUser(argEntity: NewSQLEntity? = null, userId: Long? = null, nickname: String? = null, targetName: String? = null, argTargetId: Int? = null): Boolean {
        val entity = argEntity ?:
        if (userId != null) NewMySQLIntegration.getLinkedEntity(userId) ?: return false
        else if (nickname != null) NewMySQLIntegration.getLinkedEntityByNickname(nickname) ?: return false
        else return false
        val targetId = argTargetId ?: when (targetName?.lowercase()?:return false) {
            "admin" -> 0
            "player" -> 1
            "requester" -> 2
            else -> 3
        }
        entity.accountType = targetId
        return entity.accountType==targetId
    }

    fun checkPermissionToExecute(
        message: TgMessage,
        entity: NewSQLEntity = NewMySQLIntegration.getOrRegisterLinkedEntity(message.from!!.id),
        allowedAccountTypes: List<Int> = listOf(0),
        allowedIfSpendByItself: Boolean = false,
    ): Boolean =
        !(entity.accountType in allowedAccountTypes && (!allowedIfSpendByItself || message.from!!.id==entity.userId))

    fun matchEntityFromUpdateServerPlayerStatusCommand(msg: TgMessage, allowedIfSpendByItself: Boolean = false): NewSQLEntity? {
        val args = msg.text!!.split(" ")
        val isArgUserId = if (args.size > 1) args[1].matches("[0-9]+".toRegex()) && args[1].length == 10 else false
        val isReplyToMessage = msg.replyToMessage != null
        val isItLegalReply = isReplyToMessage && msg.replyToMessage!!.messageId != config.targetTopicId
        val entity =
            if (isArgUserId)
                NewMySQLIntegration.getLinkedEntity(args[1].toLong())
            else if (isItLegalReply)
                NewMySQLIntegration.getLinkedEntity(msg.replyToMessage!!.from?.id ?: return null)
            else if (!isReplyToMessage && args.size>1 && args[1].matches("[a-zA-Z0-9_]+".toRegex()) && args[1].length in 3..16)
                NewMySQLIntegration.getLinkedEntityByNickname(args[1])
            else if (allowedIfSpendByItself)
                NewMySQLIntegration.getLinkedEntity(msg.from!!.id)
            else null
        return entity
    }

    fun updateServerPlayerStatus(
        entity: NewSQLEntity,
        applyAccountStatuses: List<String> = listOf("admin", "player", "old", "banned", "frozen"),
        targetAccountStatus: String = "player",
        editWhitelist: Boolean = false,
    ) : Boolean {
        val accountType = matchAccountTypeFromMinecraftAccountStatus(targetAccountStatus)
        val isTargetPlayer = isPlayer(accountType)
        if (!promoteUser(
                argEntity = entity,
                argTargetId = accountType,
            )
        ) return false
        else {
            entity.data?.minecraftAccounts
                ?.filter { applyAccountStatuses.contains(it.accountStatus) }
                ?.map { it.nickname }
                ?.forEach {
                    if (editWhitelist) ZixaMCTGBridge.let { main ->
                        if (isTargetPlayer) main.addToWhitelist(it)
                        else main.removeFromWhitelist(it)
                    }
                    entity.editMinecraftAccount(it, targetAccountStatus)
                }
            return true
        }
    }
    suspend fun executeUpdateServerPlayerStatusCommand(
        message: TgMessage,
        allowedExecutionAccountTypes: List<Int> = listOf(0),
        allowedExecutionIfSpendByItself: Boolean = false,
        applyAccountStatuses: List<String> = listOf("admin", "player", "old", "banned", "frozen"),
        targetAccountStatus: String = "player",
        editWhitelist: Boolean = false,
        helpText: String? = null,
        text4User: String? = null,
        text4Target: String? = null,
        removePreviousTgReplyMarkup: Boolean = true,
        additionalConsumer: suspend (Boolean, NewSQLEntity?) -> Unit = { _, _ -> },
        replyMarkup4Message4User: TgReplyMarkup? = null,
        protectContentInMessage4User: Boolean = false,
    ) : Boolean {
        var errorDueExecuting = false
        var havePermission = true
        val entity = matchEntityFromUpdateServerPlayerStatusCommand(message, allowedExecutionIfSpendByItself)
        if (entity == null) errorDueExecuting = true
        else havePermission = checkPermissionToExecute(
            message = message,
            entity = entity,
            allowedAccountTypes = allowedExecutionAccountTypes,
            allowedIfSpendByItself = allowedExecutionIfSpendByItself,
        )
        if (!havePermission) errorDueExecuting = true
        if (!errorDueExecuting && !updateServerPlayerStatus(
                entity = entity!!,
                applyAccountStatuses = applyAccountStatuses,
                targetAccountStatus = targetAccountStatus,
                editWhitelist = editWhitelist,
            )) errorDueExecuting = true
        if (errorDueExecuting && helpText != null) {
            bot.sendMessage(
                chatId = message.chat.id,
                messageThreadId = message.messageThreadId,
                text =
                    if (!havePermission) config.text.textCommandPermissionDenied
                    else helpText,
                replyParameters = TgReplyParameters(message.messageId),
            )
        } else {
            if (text4Target!=null) bot.sendMessage(
                chatId = message.chat.id,
                messageThreadId = message.messageThreadId,
                text = TextParser.formatLang(
                    text4Target,
                    "nickname" to (entity!!.nickname ?: entity.userId.toString()),
                ),
                replyParameters = TgReplyParameters(message.messageId),
            )
            var newMessage: TgMessage? = null
            try {
                if (text4User!=null) {
                    newMessage = bot.sendMessage(
                        chatId = entity!!.userId,
                        text = text4User,
                        replyMarkup = replyMarkup4Message4User,
                        protectContent = protectContentInMessage4User,
                    )
                }
            } catch (_: Exception) {}
            try {
                if (removePreviousTgReplyMarkup)
                    entity!!.data?.requests?.filter { it.request_status == "accepted" }?.forEach {
                        bot.editMessageReplyMarkup(
                            chatId = entity.userId,
                            messageId = it.message_id_in_chat_with_user.toInt(),
                            replyMarkup = TgReplyMarkup()
                        )
                }
            } catch (_: Exception) {}
            if (newMessage!=null)
                entity!!.data?.requests?.filter { it.request_status == "accepted" } ?.forEach {
//                    val request = it.copy(message_id_in_chat_with_user = newMessage.messageId.toLong())
//                    request.message_id_in_chat_with_user = newMessage.messageId.toLong()
                    entity.editRequest(it.apply { this.message_id_in_chat_with_user = newMessage.messageId.toLong() })
            }
        }
        additionalConsumer.invoke(errorDueExecuting, entity)
        return errorDueExecuting
    }

    fun matchAccountTypeFromMinecraftAccountStatus(status: String): Int = when (status) {
        "admin" -> 0
        "player" -> 1
        "banned", "frozen" -> 2
        else -> 3
    }
    fun isPlayer(accountType: Int): Boolean = accountType<=1
}