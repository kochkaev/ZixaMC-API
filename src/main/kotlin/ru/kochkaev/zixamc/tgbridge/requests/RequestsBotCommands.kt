package ru.kochkaev.zixamc.tgbridge.requests

import ru.kochkaev.zixamc.tgbridge.BotLogic
import ru.kochkaev.zixamc.tgbridge.MySQLIntegration
import ru.kochkaev.zixamc.tgbridge.RequestsBot.bot
import ru.kochkaev.zixamc.tgbridge.RequestsBot.config
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.cancelRequest
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.cancelSendingRequest
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.newRequest
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.promoteUser
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.matchEntityFromUpdateServerPlayerStatusCommand

object RequestsBotCommands {
    suspend fun onTelegramAcceptCommand(msg: TgMessage): Boolean =
        RequestsCommandLogic.executeRequestFinalAction(msg, true)
    suspend fun onTelegramRejectCommand(msg: TgMessage): Boolean  =
        RequestsCommandLogic.executeRequestFinalAction(msg, false)
    suspend fun onTelegramPromoteCommand(msg: TgMessage): Boolean {
        val entity = RequestsLogic.matchEntityFromUpdateServerPlayerStatusCommand(msg)?:return false
        if (!RequestsLogic.checkPermissionToExecute(
                msg, entity, listOf(0), false
            )) return true
        if (!promoteUser(entity)) {
            bot.sendMessage(
                chatId = msg.chat.id,
                text = BotLogic.escapePlaceholders(config.commonLang.command.promoteHelp),
                replyParameters = TgReplyParameters(msg.messageId),
            )
            return false
        } else {
            bot.sendMessage(
                chatId = msg.chat.id,
                text = BotLogic.escapePlaceholders(config.target.lang.event.onPromote, entity.nickname?:entity.userId.toString()),
                replyParameters = TgReplyParameters(msg.messageId),
            )
            return true
        }
    }
    suspend fun onTelegramRulesUpdatedCommand(msg: TgMessage): Boolean {
        val entity = MySQLIntegration.getLinkedEntity(msg.from?.id?:return false)?:return false
        if (!RequestsLogic.checkPermissionToExecute(
                msg, entity, listOf(0), false
            )) return true
        bot.sendMessage(
            chatId = config.target.chatId,
            text = BotLogic.escapePlaceholders(config.target.lang.event.onRulesUpdated),
            replyMarkup = TgInlineKeyboardMarkup(
                listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.user.lang.button.agreeWithRules,
                    callback_data = "agree_with_rules",
                ))),
            )
        )
        MySQLIntegration.linkedEntities.map {it.value} .filter { it.agreedWithRules } .forEach {
            it.agreedWithRules = false
            try {
                bot.sendMessage(
                    chatId = it.userId,
                    text = BotLogic.escapePlaceholders(config.user.lang.event.onRulesUpdated),
                    replyMarkup = TgInlineKeyboardMarkup(
                        listOf(
                            listOf(
                                TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                    text = config.user.lang.button.agreeWithRules,
                                    callback_data = "agree_with_rules",
                                )
                            )
                        ),
                    )
                )
            } catch (_: Exception) {}
        }
        return true
    }
    suspend fun onTelegramLeaveCommand(msg: TgMessage): Boolean =
        RequestsCommandLogic.executeUpdateServerPlayerStatusCommand(
            message = msg,
            allowedExecutionAccountTypes = listOf(0),
            allowedExecutionIfSpendByItself = true,
            applyAccountStatuses = listOf("admin", "player"),
            targetAccountStatus = "frozen",
            editWhitelist = true,
            helpText = config.commonLang.command.leaveHelp,
            text4User = config.user.lang.event.onLeave,
            text4Target = config.target.lang.event.onLeave,
            removePreviousTgReplyMarkup = true,
            removeProtectedContent = true,
        )
    suspend fun onTelegramReturnCommand(msg: TgMessage): Boolean =
        RequestsCommandLogic.executeUpdateServerPlayerStatusCommand(
            message = msg,
            allowedExecutionAccountTypes = listOf(0),
            allowedExecutionIfSpendByItself = false,
            applyAccountStatuses = listOf("frozen"),
            targetAccountStatus = "player",
            editWhitelist = true,
            helpText = config.commonLang.command.returnHelp,
            text4User = config.user.lang.event.onReturn,
            text4Target = config.target.lang.event.onReturn,
            removePreviousTgReplyMarkup = true,
            replyMarkup4Message = TgInlineKeyboardMarkup(
                listOf(
                    listOf(
                        TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.user.lang.button.joinToPlayersGroup,
                            url = config.playersGroupInviteLink
                        )
                    )
                )
            ),
            protectContentInMessage = true,
        )
    suspend fun onTelegramKickCommand(msg: TgMessage): Boolean =
        RequestsCommandLogic.executeUpdateServerPlayerStatusCommand(
            message = msg,
            allowedExecutionAccountTypes = listOf(0),
            allowedExecutionIfSpendByItself = false,
            applyAccountStatuses = listOf("admin", "player", "frozen"),
            targetAccountStatus = "banned",
            editWhitelist = true,
            helpText = config.commonLang.command.kickHelp,
            text4User = config.user.lang.event.onKick,
            text4Target = config.target.lang.event.onKick,
            removePreviousTgReplyMarkup = true,
            additionalConsumer = { hasError, entity ->
                if (!hasError) try {
                    bot.banChatMember(msg.chat.id, entity!!.userId)
                } catch (_: Exception) {}
            },
            removeProtectedContent = true,
        )
    suspend fun onTelegramRestrictCommand(message: TgMessage): Boolean {
        val entity = MySQLIntegration.getLinkedEntityByTempArrayMessagesId(message.replyToMessage?.messageId?.toLong()?:0)
            ?: matchEntityFromUpdateServerPlayerStatusCommand(message, false)
        val errorDueExecuting = RequestsLogic.executeCheckPermissionsAndExceptions(
            message = message,
            entity = entity,
            allowedExecutionAccountTypes = listOf(0),
            allowedExecutionIfSpendByItself = false,
            applyAccountStatuses = listOf("admin", "player", "frozen"),
            targetAccountStatus = "banned",
            targetAccountType = 3,
            editWhitelist = true,
            helpText = config.commonLang.command.restrictHelp,
        )
        if (!errorDueExecuting) {
            val text4Target = config.target.lang.event.onRestrict
            if (text4Target.isNotEmpty()) bot.sendMessage(
                chatId = message.chat.id,
                text = BotLogic.escapePlaceholders(text4Target, entity!!.nickname ?: entity.userId.toString()),
                replyParameters = TgReplyParameters(message.messageId),
            )
            var newMessage: TgMessage? = null
            try {
                val text4User = config.user.lang.event.onRestrict
                if (text4User.isNotEmpty()) {
                    newMessage = bot.sendMessage(
                        chatId = entity!!.userId,
                        text = BotLogic.escapePlaceholders(text4User, entity.nickname ?: entity.userId.toString()),
                    )
                }
            } catch (_: Exception) {}
            try {
                entity!!.data?.requests?.filter { it.request_status == "accepted" }?.forEach {
                    bot.editMessageReplyMarkup(
                        chatId = entity.userId,
                        messageId = it.message_id_in_chat_with_user.toInt(),
                        replyMarkup = TgReplyMarkup()
                    )
                }
            } catch (_: Exception) {}
            BotLogic.deleteAllProtected(entity!!.data?.protectedMessages?:listOf(), 3)
            entity.data = entity.data.let { it!!.requests = ArrayList(it.requests.filter { it1 -> it1.request_status != "creating" }); it }
            entity.data?.requests?.firstOrNull { it.request_status == "pending" } ?.let {
                entity.editRequest(it.apply { this.request_status = "rejected" })
            }
            if (newMessage!=null)
                entity.data?.requests?.filter { it.request_status == "accepted" } ?.forEach {
                    entity.editRequest(it.apply { this.message_id_in_chat_with_user = newMessage.messageId.toLong() })
                }
            try {
                bot.banChatMember(message.chat.id, entity.userId)
            } catch (_: Exception) {}
            entity.isRestricted = true
        }
        return errorDueExecuting
    }
    suspend fun onTelegramStartCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        val entity = MySQLIntegration.getOrAddUser(msg.from?.id?:return false)
        if (entity.isRestricted) return false
        bot.sendMessage(
            chatId = msg.chat.id,
            text = config.user.lang.event.onStart,
            replyMarkup = TgInlineKeyboardMarkup(listOf(listOf(
                TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                text = config.user.lang.button.createRequest,
                callback_data = "create_request",
            ))))
        )
        return true
    }
    suspend fun onTelegramNewCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        if (msg.from == null) return false
        val entity = MySQLIntegration.getLinkedEntity(msg.from.id)?:return false
        if (entity.isRestricted) return false
        return newRequest(entity)
    }
    suspend fun onTelegramCancelCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        val entity = MySQLIntegration.getLinkedEntity(msg.from?.id?:return false)?:return false
        val requests = (entity.data?:return false).requests
        if (requests.any {it.request_status == "pending"}) return cancelRequest(entity)
        else if (requests.any {it.request_status == "creating"}) return cancelSendingRequest(entity)
        return false
    }
}