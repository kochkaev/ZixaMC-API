package ru.kochkaev.zixamc.tgbridge.requests

import ru.kochkaev.zixamc.tgbridge.NewMySQLIntegration
import ru.kochkaev.zixamc.tgbridge.RequestsBot.bot
import ru.kochkaev.zixamc.tgbridge.RequestsBot.config
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.cancelRequest
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.cancelSendingRequest
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.newRequest
import ru.kochkaev.zixamc.tgbridge.requests.RequestsLogic.promoteUser
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.MinecraftAccountData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters

object RequestsBotCommands {
    suspend fun onTelegramAcceptCommand(msg: TgMessage): Boolean {
        if (msg.chat.id >= 0 || !NewMySQLIntegration.isAdmin(msg.from?.id?:return false)) return true
        val replied = msg.replyToMessage?:return false
//        if (replied.from?.id != bot.me.id || replied.forwardOrigin == null) return false
        val entity = NewMySQLIntegration.getLinkedEntityByTempArrayMessagesId(replied.messageId.toLong())?:return false
        val request = entity.data!!.requests.firstOrNull {it.request_status == "pending"} ?: return false
        bot.sendMessage(
            chatId = config.targetChatId,
            text = config.text.textOnAccept4Target.replace("{nickname}", "${request.request_nickname}"),
            replyParameters = TgReplyParameters(replied.messageId),
        )
        val newMessage = bot.sendMessage(
            chatId = entity.userId,
            text = config.text.textOnAccept4User,
            replyParameters = TgReplyParameters(request.message_id_in_chat_with_user.toInt()),
            replyMarkup = TgInlineKeyboardMarkup(listOf(listOf(
                TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.textButtonJoinToPlayersGroup,
                    url = config.playersGroupInviteLink
                )
            ))),
            protectContent = true,
        )
        bot.editMessageReplyMarkup(
            chatId = entity.userId,
            messageId = request.message_id_in_chat_with_user.toInt(),
            replyMarkup = TgReplyMarkup()
        )
        request.request_status = "accepted"
        request.message_id_in_chat_with_user = newMessage.messageId.toLong()
        entity.editRequest(request)
        entity.accountType = 1
        entity.tempArray = arrayOf()
        entity.addMinecraftAccount(MinecraftAccountData(request.request_nickname!!, "player"))
        ZixaMCTGBridge.addToWhitelist(request.request_nickname!!)
        return true
    }
    suspend fun onTelegramRejectCommand(msg: TgMessage): Boolean {
        if (msg.chat.id >= 0 || !NewMySQLIntegration.isAdmin(msg.from?.id?:return false)) return true
        val replied = msg.replyToMessage?:return false
        if (replied.from?.id != bot.me.id || replied.forwardOrigin == null) return false
        val entity = NewMySQLIntegration.getLinkedEntityByTempArrayMessagesId(replied.messageId.toLong())?:return false
        val request = entity.data!!.requests.firstOrNull {it.request_status == "pending"} ?: return false
        bot.sendMessage(
            chatId = config.targetChatId,
            text = config.text.textOnReject4Target.replace("{nickname}", "${request.request_nickname}"),
            replyParameters = TgReplyParameters(replied.messageId),
        )
        bot.sendMessage(
            chatId = entity.userId,
            text = config.text.textOnReject4User,
            replyParameters = TgReplyParameters(request.message_id_in_chat_with_user.toInt()),
        )
        bot.editMessageReplyMarkup(
            chatId = entity.userId,
            messageId = request.message_id_in_chat_with_user.toInt(),
            replyMarkup = TgReplyMarkup()
        )
        request.request_status = "rejected"
        entity.editRequest(request)
        entity.tempArray = arrayOf()
        return true
    }
    suspend fun onTelegramPromoteCommand(msg: TgMessage): Boolean {
        if (!NewMySQLIntegration.isAdmin(msg.from?.id?:return false)) return true
        val args = msg.text?.split(" ")?:return false
        val isNotArgsPassed = args.size > 1
        val isArgUserId = if (isNotArgsPassed) args[1].matches("[0-9]+".toRegex()) && args[1].length == 10 else false
        val isReplyToMessage = msg.replyToMessage != null && args.size == 2
        val isArgTargetId = if (isNotArgsPassed) args[if (isReplyToMessage) 1 else 2].matches("[0-9]+".toRegex()) else false
        if (!(args.size == 3 && !isReplyToMessage || isReplyToMessage && args.size == 2) || !promoteUser(
                userId =
                    if (isArgUserId) args[1].toLong()
//                else if (args[2].startsWith("@")) msg.entities?.firstOrNull {it.type == "mention" }?.
                    else if (isReplyToMessage) msg.replyToMessage!!.from?.id
                    else null,
                nickname = if (args[1].matches("[a-zA-Z0-9_]+".toRegex()) && args[1].length in 3..16 && !isArgUserId) args[1] else null,
                targetName = if (!isArgTargetId) args[if (isReplyToMessage) 1 else 2] else null,
                argTargetId = if (isArgTargetId) args[if (isReplyToMessage) 1 else 2].toInt() else null,
            )
        ) {
            bot.sendMessage(
                chatId = msg.chat.id,
                messageThreadId = msg.messageThreadId,
                text = config.text.textSyntaxPromoteHelp,
                replyParameters = TgReplyParameters(msg.messageId),
            )
            return false
        } else {
            bot.sendMessage(
                chatId = msg.chat.id,
                messageThreadId = msg.messageThreadId,
                text = config.text.textOnPromote4Target,
                replyParameters = TgReplyParameters(msg.messageId),
            )
            return true
        }
    }
    suspend fun onTelegramRulesUpdatedCommand(msg: TgMessage): Boolean {
        bot.sendMessage(
            chatId = config.targetChatId,
            text = config.text.textOnRulesUpdated4Target,
            replyMarkup = TgInlineKeyboardMarkup(
                listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.textButtonAgreeWithRules,
                    callback_data = "agree_with_rules",
                ))),
            )
        )
        NewMySQLIntegration.getAllRegisteredUserIds().forEach {
            NewMySQLIntegration.setAgreedWithRules(it, false)
            bot.sendMessage(
                chatId = it,
                text = config.text.textOnRulesUpdated4User,
                replyMarkup = TgInlineKeyboardMarkup(
                    listOf(listOf(
                        TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                        text = config.text.textButtonAgreeWithRules,
                        callback_data = "agree_with_rules",
                    ))),
                )
            )
        }
        return true
    }
    suspend fun onTelegramLeaveCommand(msg: TgMessage): Boolean =
        RequestsLogic.executeUpdateServerPlayerStatusCommand(
            message = msg,
            allowedExecutionAccountTypes = listOf(0),
            allowedExecutionIfSpendByItself = true,
            applyAccountStatuses = listOf("admin", "player"),
            targetAccountStatus = "frozen",
            editWhitelist = true,
            helpText = config.text.textSyntaxLeavedHelp,
            text4User = config.text.textOnLeave4User,
            text4Target = config.text.textOnLeave4Target,
            removePreviousTgReplyMarkup = true,
        )
    suspend fun onTelegramReturnCommand(msg: TgMessage): Boolean =
        RequestsLogic.executeUpdateServerPlayerStatusCommand(
            message = msg,
            allowedExecutionAccountTypes = listOf(0),
            allowedExecutionIfSpendByItself = true,
            applyAccountStatuses = listOf("frozen"),
            targetAccountStatus = "player",
            editWhitelist = true,
            helpText = config.text.textSyntaxReturnHelp,
            text4User = config.text.textOnReturn4User,
            text4Target = config.text.textOnReturn4Target,
            removePreviousTgReplyMarkup = true,
            replyMarkup4Message4User = TgInlineKeyboardMarkup(
                listOf(
                    listOf(
                        TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.text.textButtonJoinToPlayersGroup,
                            url = config.playersGroupInviteLink
                        )
                    )
                )
            ),
            protectContentInMessage4User = true,
        )
    suspend fun onTelegramKickCommand(msg: TgMessage): Boolean =
        RequestsLogic.executeUpdateServerPlayerStatusCommand(
            message = msg,
            allowedExecutionAccountTypes = listOf(0),
            allowedExecutionIfSpendByItself = false,
            applyAccountStatuses = listOf("admin", "player", "frozen"),
            targetAccountStatus = "banned",
            editWhitelist = true,
            helpText = config.text.textSyntaxKickHelp,
            text4User = config.text.textOnKick4User,
            text4Target = config.text.textOnKick4Target,
            removePreviousTgReplyMarkup = true,
            additionalConsumer = { hasError, entity ->
                if (!hasError) bot.banChatMember(msg.chat.id, entity!!.userId)
            }
        )
    suspend fun onTelegramStartCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        NewMySQLIntegration.addUser(msg.from?.id?:return false)
        bot.sendMessage(
            chatId = msg.chat.id,
            text = config.text.textOnStart,
            replyMarkup = TgInlineKeyboardMarkup(listOf(listOf(
                TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                text = config.text.textButtonCreateRequest,
                callback_data = "create_request",
            ))))
        )
        return true
    }
    suspend fun onTelegramNewCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        if (msg.from == null) return false
        val entity = NewMySQLIntegration.getLinkedEntity(msg.from.id)?:return false
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