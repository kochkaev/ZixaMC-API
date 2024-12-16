package ru.kochkaev.zixamc.tgbridge.requests

import ru.kochkaev.zixamc.tgbridge.BotLogic
import ru.kochkaev.zixamc.tgbridge.NewMySQLIntegration
import ru.kochkaev.zixamc.tgbridge.NewSQLEntity
import ru.kochkaev.zixamc.tgbridge.RequestsBot.bot
import ru.kochkaev.zixamc.tgbridge.RequestsBot.config
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.MinecraftAccountData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.checkPermissionToExecute
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.matchAccountTypeFromMinecraftAccountStatus
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.matchEntityFromUpdateServerPlayerStatusCommand
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.updateServerPlayerStatus

object RequestsCommandLogic {

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
        removeProtectedContent: Boolean = false,
    ) : Boolean {
        val entity = matchEntityFromUpdateServerPlayerStatusCommand(message, allowedExecutionIfSpendByItself)
        val errorDueExecuting = RequestsLogic.executeCheckPermissionsAndExceptions(
            message = message,
            entity = entity,
            allowedExecutionAccountTypes = allowedExecutionAccountTypes,
            allowedExecutionIfSpendByItself = allowedExecutionIfSpendByItself,
            applyAccountStatuses = applyAccountStatuses,
            targetAccountStatus = targetAccountStatus,
            editWhitelist = editWhitelist,
            helpText = helpText,
        )
        if (!errorDueExecuting) {
            if (text4Target!=null) bot.sendMessage(
                chatId = message.chat.id,
                text = BotLogic.escapePlaceholders(text4Target, entity!!.nickname ?: entity.userId.toString()),
                replyParameters = TgReplyParameters(message.messageId),
            )
            var newMessage: TgMessage? = null
            try {
                if (text4User!=null) {
                    newMessage = bot.sendMessage(
                        chatId = entity!!.userId,
                        text = BotLogic.escapePlaceholders(text4User, entity.nickname ?: entity.userId.toString()),
                        replyMarkup = replyMarkup4Message4User,
                        protectContent = protectContentInMessage4User,
                    )
                    if (protectContentInMessage4User) entity.setProtectedInfoMessage(
                        message = newMessage,
                        protectedType = "text",
                        protectLevel = 1,
                        senderBotId = bot.me.id,
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
            if (removeProtectedContent)
                BotLogic.deleteAllProtected(entity!!.data?.protectedMessages?:listOf(), matchAccountTypeFromMinecraftAccountStatus(targetAccountStatus))
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

    suspend fun executeRequestFinalAction(
        message: TgMessage,
        isAccepted: Boolean,
    ) : Boolean {
        if (message.chat.id >= 0) return true
        val replied = message.replyToMessage?:return false
        val entity = NewMySQLIntegration.getLinkedEntityByTempArrayMessagesId(replied.messageId.toLong())?:return false
        if (!checkPermissionToExecute(
                message, entity, listOf(0), false
            )) return true
        val request = entity.data!!.requests.firstOrNull {it.request_status == "pending"} ?: return false
        val message4User = BotLogic.escapePlaceholders(
            text = if (isAccepted) config.text.events.forUser.textOnAccept4User else config.text.events.forUser.textOnReject4User,
            nickname = request.request_nickname,
        )
        val message4Target = BotLogic.escapePlaceholders(
            text = if (isAccepted) config.text.events.forTarget.textOnAccept4Target else config.text.events.forTarget.textOnReject4Target,
            nickname = request.request_nickname,
        )
        bot.sendMessage(
            chatId = config.targetChatId,
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
        request.request_status = if (isAccepted) "accepted" else "rejected"
        request.message_id_in_chat_with_user = newMessage.messageId.toLong()
        entity.editRequest(request)
        entity.tempArray = arrayOf()
        if (isAccepted) {
            RequestsLogic.sendOnJoinInfoMessage(entity, newMessage.messageId)
            entity.accountType = 1
            entity.addMinecraftAccount(MinecraftAccountData(request.request_nickname!!, "player"))
            ZixaMCTGBridge.addToWhitelist(request.request_nickname!!)
        }
        return true
    }
}