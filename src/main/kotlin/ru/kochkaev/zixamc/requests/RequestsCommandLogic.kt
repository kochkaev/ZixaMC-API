package ru.kochkaev.zixamc.requests

import ru.kochkaev.zixamc.api.sql.data.NewProtectedData
import ru.kochkaev.zixamc.api.sql.data.RequestType
import ru.kochkaev.zixamc.api.telegram.BotLogic
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.data.MinecraftAccountData
import ru.kochkaev.zixamc.api.sql.data.MinecraftAccountType
import ru.kochkaev.zixamc.requests.RequestsBot.bot
import ru.kochkaev.zixamc.requests.RequestsBot.config
import ru.kochkaev.zixamc.api.telegram.model.TgMessage
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters

object RequestsCommandLogic {

    suspend fun executeUpdateServerPlayerStatusCommand(
        message: TgMessage?,
        allowedExecutionAccountTypes: List<AccountType> = listOf(AccountType.ADMIN),
        allowedExecutionIfSpendByItself: Boolean = false,
        applyAccountStatuses: List<MinecraftAccountType> = MinecraftAccountType.getAll(),
        targetAccountStatus: MinecraftAccountType = MinecraftAccountType.PLAYER,
        editWhitelist: Boolean = false,
        helpText: String? = null,
        text4User: String? = null,
        text4Target: String? = null,
        removePreviousTgReplyMarkup: Boolean = true,
        additionalConsumer: suspend (Boolean, SQLUser?) -> Unit = { _, _ -> },
        replyMarkup4Message: TgReplyMarkup? = null,
        protectContentInMessage: Boolean = false,
        removeProtectedContent: Boolean = false,
        entity: SQLUser? = RequestsLogic.matchEntityFromUpdateServerPlayerStatusCommand(
            message,
            allowedExecutionIfSpendByItself
        ),
        entityExecutor: SQLUser? = if (message!=null) SQLUser.get(message.from!!.id) else null,
        messageForReplyId: Int? = message?.messageId,
    ) : Boolean {
        val errorDueExecuting = RequestsLogic.executeCheckPermissionsAndExceptions(
            message = message,
            entity = entity,
            entityExecutor = entityExecutor,
            allowedExecutionAccountTypes = allowedExecutionAccountTypes,
            allowedExecutionIfSpendByItself = allowedExecutionIfSpendByItself,
            applyAccountStatuses = applyAccountStatuses,
            targetAccountStatus = targetAccountStatus,
            editWhitelist = editWhitelist,
            helpText = helpText,
        )
        if (!errorDueExecuting) {
            if (text4Target!=null) bot.sendMessage(
                chatId = config.target.chatId,
                text = BotLogic.escapePlaceholders(text4Target, entity!!.nickname ?: entity.userId.toString()),
                replyParameters = if (messageForReplyId!=null) TgReplyParameters(
                    messageForReplyId
                ) else null,
            )
            var newMessage: TgMessage? = null
            try {
                if (text4User!=null) {
                    newMessage = bot.sendMessage(
                        chatId = entity!!.userId,
                        text = BotLogic.escapePlaceholders(text4User, entity.nickname ?: entity.userId.toString()),
                        replyMarkup = replyMarkup4Message,
                        protectContent = protectContentInMessage,
                    )
                    if (protectContentInMessage) entity.setProtectedInfoMessage(
                        message = newMessage,
                        protectedType = NewProtectedData.ProtectedType.TEXT,
                        protectLevel = AccountType.PLAYER,
                        senderBotId = bot.me.id,
                    )
                }
            } catch (_: Exception) {}
            try {
                if (removePreviousTgReplyMarkup)
                    entity!!.data.getCasted(RequestsChatDataType)?.filter { it.request_status == RequestType.ACCEPTED } ?.forEach {
                        bot.editMessageReplyMarkup(
                            chatId = entity.userId,
                            messageId = it.message_id_in_chat_with_user.toInt(),
                            replyMarkup = TgReplyMarkup()
                        )
                    }
            } catch (_: Exception) {}
            if (removeProtectedContent)
                entity!!.deleteProtected(targetAccountStatus.toAccountType())
            if (newMessage!=null)
                entity!!.data.getCasted(RequestsChatDataType)?.filter { it.request_status == RequestType.ACCEPTED } ?.forEach {
//                    val request = it.copy(message_id_in_chat_with_user = newMessage.messageId.toLong())
//                    request.message_id_in_chat_with_user = newMessage.messageId.toLong()
                    entity.editRequest(it.apply { this.message_id_in_chat_with_user = newMessage.messageId.toLong() })
                }
        }
        additionalConsumer.invoke(errorDueExecuting, entity)
        return errorDueExecuting
    }

    suspend fun executeRequestFinalAction(
        message: TgMessage,
        isAccepted: Boolean,
    ) : Boolean {
        if (message.chat.id >= 0) return true
        val replied = message.replyToMessage?:return false
        val entity = SQLUser.getByTempArray(replied.messageId.toString())?:return false
        if (!RequestsLogic.checkPermissionToExecute(
                message, entity, listOf(AccountType.ADMIN), false
            )
        ) return true
        val request = entity.data.getCasted(RequestsChatDataType)?.firstOrNull {it.request_status == RequestType.PENDING} ?: return false
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
            replyParameters = TgReplyParameters(replied.messageId),
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
            RequestsLogic.sendOnJoinInfoMessage(entity, newMessage.messageId)
            entity.accountType = AccountType.PLAYER
            entity.addMinecraftAccount(MinecraftAccountData(request.request_nickname!!, MinecraftAccountType.PLAYER))
            WhitelistManager.add(request.request_nickname!!)
        }
        return true
    }
}