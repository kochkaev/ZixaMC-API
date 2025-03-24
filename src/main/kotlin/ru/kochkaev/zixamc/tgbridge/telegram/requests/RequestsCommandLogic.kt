package ru.kochkaev.zixamc.tgbridge.telegram.requests

import ru.kochkaev.zixamc.tgbridge.telegram.BotLogic
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot.config
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgMessage
import ru.kochkaev.zixamc.tgbridge.telegram.requests.RequestsLogic.checkPermissionToExecute
import ru.kochkaev.zixamc.tgbridge.telegram.requests.RequestsLogic.matchEntityFromUpdateServerPlayerStatusCommand
import ru.kochkaev.zixamc.tgbridge.sql.data.*

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
        additionalConsumer: suspend (Boolean, SQLEntity?) -> Unit = { _, _ -> },
        replyMarkup4Message: ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyMarkup? = null,
        protectContentInMessage: Boolean = false,
        removeProtectedContent: Boolean = false,
        entity: SQLEntity? = matchEntityFromUpdateServerPlayerStatusCommand(message, allowedExecutionIfSpendByItself),
        entityExecutor: SQLEntity? = if (message!=null) SQLEntity.get(message.from!!.id) else null,
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
                replyParameters = if (messageForReplyId!=null) ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyParameters(
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
                    entity!!.data.requests.filter { it.request_status == RequestType.ACCEPTED }.forEach {
                        bot.editMessageReplyMarkup(
                            chatId = entity.userId,
                            messageId = it.message_id_in_chat_with_user.toInt(),
                            replyMarkup = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyMarkup()
                        )
                    }
            } catch (_: Exception) {}
            if (removeProtectedContent)
                entity!!.deleteProtected(targetAccountStatus.toAccountType())
            if (newMessage!=null)
                entity!!.data.requests.filter { it.request_status == RequestType.ACCEPTED } .forEach {
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
        val entity = SQLEntity.getByTempArray(replied.messageId.toString())?:return false
        if (!checkPermissionToExecute(
                message, entity, listOf(AccountType.ADMIN), false
            )) return true
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
            replyParameters = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyParameters(replied.messageId),
        )
        val newMessage = bot.sendMessage(
            chatId = entity.userId,
            text = message4User,
            replyParameters = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyParameters(request.message_id_in_chat_with_user.toInt()),
            protectContent = false,
        )
        bot.editMessageReplyMarkup(
            chatId = entity.userId,
            messageId = request.message_id_in_chat_with_user.toInt(),
            replyMarkup = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyMarkup()
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