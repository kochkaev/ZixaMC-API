package ru.kochkaev.zixamc.tgbridge.requests

import ru.kochkaev.zixamc.tgbridge.BotLogic
import ru.kochkaev.zixamc.tgbridge.NewMySQLIntegration
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
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.checkPermissionToExecute
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.matchEntityFromUpdateServerPlayerStatusCommand
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.updateServerPlayerStatus

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
                text = BotLogic.escapePlaceholders(config.text.commands.textSyntaxPromoteHelp),
                replyParameters = TgReplyParameters(msg.messageId),
            )
            return false
        } else {
            bot.sendMessage(
                chatId = msg.chat.id,
                text = BotLogic.escapePlaceholders(config.text.events.forTarget.textOnPromote4Target, entity.nickname?:entity.userId.toString()),
                replyParameters = TgReplyParameters(msg.messageId),
            )
            return true
        }
    }
    suspend fun onTelegramRulesUpdatedCommand(msg: TgMessage): Boolean {
        val entity = NewMySQLIntegration.getLinkedEntity(msg.from?.id?:return false)?:return false
        if (!RequestsLogic.checkPermissionToExecute(
                msg, entity, listOf(0), false
            )) return true
        bot.sendMessage(
            chatId = config.targetChatId,
            text = BotLogic.escapePlaceholders(config.text.events.forTarget.textOnRulesUpdated4Target),
            replyMarkup = TgInlineKeyboardMarkup(
                listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.buttons.textButtonAgreeWithRules,
                    callback_data = "agree_with_rules",
                ))),
            )
        )
        NewMySQLIntegration.linkedEntities.map {it.value} .filter { it.agreedWithRules } .forEach {
            it.agreedWithRules = false
            try {
                bot.sendMessage(
                    chatId = it.userId,
                    text = BotLogic.escapePlaceholders(config.text.events.forUser.textOnRulesUpdated4User),
                    replyMarkup = TgInlineKeyboardMarkup(
                        listOf(
                            listOf(
                                TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                    text = config.text.buttons.textButtonAgreeWithRules,
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
            helpText = config.text.commands.textSyntaxLeavedHelp,
            text4User = config.text.events.forUser.textOnLeave4User,
            text4Target = config.text.events.forTarget.textOnLeave4Target,
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
            helpText = config.text.commands.textSyntaxReturnHelp,
            text4User = config.text.events.forUser.textOnReturn4User,
            text4Target = config.text.events.forTarget.textOnReturn4Target,
            removePreviousTgReplyMarkup = true,
            replyMarkup4Message4User = TgInlineKeyboardMarkup(
                listOf(
                    listOf(
                        TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.text.buttons.textButtonJoinToPlayersGroup,
                            url = config.playersGroupInviteLink
                        )
                    )
                )
            ),
            protectContentInMessage4User = true,
        )
    suspend fun onTelegramKickCommand(msg: TgMessage): Boolean =
        RequestsCommandLogic.executeUpdateServerPlayerStatusCommand(
            message = msg,
            allowedExecutionAccountTypes = listOf(0),
            allowedExecutionIfSpendByItself = false,
            applyAccountStatuses = listOf("admin", "player", "frozen"),
            targetAccountStatus = "banned",
            editWhitelist = true,
            helpText = config.text.commands.textSyntaxKickHelp,
            text4User = config.text.events.forUser.textOnKick4User,
            text4Target = config.text.events.forTarget.textOnKick4Target,
            removePreviousTgReplyMarkup = true,
            additionalConsumer = { hasError, entity ->
                if (!hasError) try {
                    bot.banChatMember(msg.chat.id, entity!!.userId)
                } catch (_: Exception) {}
            },
            removeProtectedContent = true,
        )
    suspend fun onTelegramRestrictCommand(message: TgMessage): Boolean {
        val entity = NewMySQLIntegration.getLinkedEntityByTempArrayMessagesId(message.replyToMessage?.messageId?.toLong()?:0)
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
            helpText = config.text.commands.textSyntaxRestrictHelp,
        )
        if (!errorDueExecuting) {
            val text4Target = config.text.events.forTarget.textOnRestrict4Target
            if (text4Target.isNotEmpty()) bot.sendMessage(
                chatId = message.chat.id,
                text = BotLogic.escapePlaceholders(text4Target, entity!!.nickname ?: entity.userId.toString()),
                replyParameters = TgReplyParameters(message.messageId),
            )
            var newMessage: TgMessage? = null
            try {
                val text4User = config.text.events.forUser.textOnRestrict4User
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
        val entity = NewMySQLIntegration.getOrAddUser(msg.from?.id?:return false)
        if (entity.isRestricted) return false
        bot.sendMessage(
            chatId = msg.chat.id,
            text = config.text.events.forUser.textOnStart,
            replyMarkup = TgInlineKeyboardMarkup(listOf(listOf(
                TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                text = config.text.buttons.textButtonCreateRequest,
                callback_data = "create_request",
            ))))
        )
        return true
    }
    suspend fun onTelegramNewCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        if (msg.from == null) return false
        val entity = NewMySQLIntegration.getLinkedEntity(msg.from.id)?:return false
        if (entity.isRestricted) return false
        return newRequest(entity)
    }
    suspend fun onTelegramCancelCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        val entity = NewMySQLIntegration.getLinkedEntity(msg.from?.id?:return false)?:return false
        val requests = (entity.data?:return false).requests
        if (requests.any {it.request_status == "pending"}) return cancelRequest(entity)
        else if (requests.any {it.request_status == "creating"}) return cancelSendingRequest(entity)
        return false
    }
}