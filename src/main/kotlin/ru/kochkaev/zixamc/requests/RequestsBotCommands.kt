package ru.kochkaev.zixamc.requests

import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.telegram.BotLogic
import ru.kochkaev.zixamc.requests.RequestsBot.bot
import ru.kochkaev.zixamc.requests.RequestsBot.config
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.data.MinecraftAccountType
import ru.kochkaev.zixamc.api.sql.data.RequestType
import ru.kochkaev.zixamc.api.telegram.model.TgMessage
import ru.kochkaev.zixamc.requests.RequestsLogic.matchEntityFromUpdateServerPlayerStatusCommand
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.telegram.ServerBot
import ru.kochkaev.zixamc.api.telegram.model.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters
import ru.kochkaev.zixamc.api.sql.feature.FeatureTypes

object RequestsBotCommands {
    suspend fun onTelegramAcceptCommand(msg: TgMessage): Boolean {
//        RequestsCommandLogic.executeRequestFinalAction(msg, true)
        return RequestsLogic.executeRequestFinalAction(
            entity = SQLUser.get(msg.from?.id ?: return false) ?: return false,
            isAccepted = true,
        )
    }
    suspend fun onTelegramRejectCommand(msg: TgMessage): Boolean {
//        RequestsCommandLogic.executeRequestFinalAction(msg, false)
        return RequestsLogic.executeRequestFinalAction(
            entity = SQLUser.get(msg.from?.id ?: return false) ?: return false,
            isAccepted = false,
        )
    }
    suspend fun onTelegramPromoteCommand(msg: TgMessage): Boolean {
        val entity = matchEntityFromUpdateServerPlayerStatusCommand(msg) ?:return false
        if (!RequestsLogic.checkPermissionToExecute(
                msg, entity, listOf(AccountType.ADMIN), false
            )) return true
        if (!RequestsLogic.promoteUser(entity)) {
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
        val entity = SQLUser.get(msg.from?.id?:return false)?:return false
        return RequestsLogic.updateRules(entity, msg.messageId, false)
    }
    suspend fun onTelegramRulesUpdatedWithRevokeCommand(msg: TgMessage): Boolean {
        val entity = SQLUser.get(msg.from?.id?:return false)?:return false
        return RequestsLogic.updateRules(entity, msg.messageId, true)
    }
    suspend fun onTelegramLeaveCommand(msg: TgMessage): Boolean =
        RequestsCommandLogic.executeUpdateServerPlayerStatusCommand(
            message = msg,
            allowedExecutionAccountTypes = listOf(AccountType.ADMIN),
            allowedExecutionIfSpendByItself = true,
            applyAccountStatuses = MinecraftAccountType.getAllActiveNow(),
            targetAccountStatus = MinecraftAccountType.FROZEN,
            editWhitelist = true,
            helpText = config.commonLang.command.leaveHelp,
            text4User = config.user.lang.event.onLeave,
            text4Target = config.target.lang.event.onLeave,
            removePreviousTgReplyMarkup = true,
//            additionalConsumer = { hasError, entity ->
//                if (!hasError) try {
//                    bot.banChatMember(msg.chat.id, entity!!.userId)
//                } catch (_: Exception) {}
//            },
            removeProtectedContent = true,
        )
    suspend fun onTelegramReturnCommand(msg: TgMessage): Boolean =
        RequestsCommandLogic.executeUpdateServerPlayerStatusCommand(
            message = msg,
            allowedExecutionAccountTypes = listOf(AccountType.ADMIN),
            allowedExecutionIfSpendByItself = false,
            applyAccountStatuses = listOf(MinecraftAccountType.FROZEN),
            targetAccountStatus = MinecraftAccountType.PLAYER,
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
            additionalConsumer = { hasError, entity ->
                if (!hasError) SQLGroup.getAllWithFeature(FeatureTypes.PLAYERS_GROUP).forEach {
                    try {
                        bot.unbanChatMember(it.chatId, entity!!.userId, true)
                    } catch (_: Exception) { try {
                        ServerBot.bot.unbanChatMember(it.chatId, entity!!.userId, true)
                    } catch (_: Exception) {} }
                }
            },
            protectContentInMessage = true,
        )
    suspend fun onTelegramKickCommand(msg: TgMessage): Boolean =
        RequestsCommandLogic.executeUpdateServerPlayerStatusCommand(
            message = msg,
            allowedExecutionAccountTypes = listOf(AccountType.ADMIN),
            allowedExecutionIfSpendByItself = false,
            applyAccountStatuses = MinecraftAccountType.getAllMaybeActive(),
            targetAccountStatus = MinecraftAccountType.BANNED,
            editWhitelist = true,
            helpText = config.commonLang.command.kickHelp,
            text4User = config.user.lang.event.onKick,
            text4Target = config.target.lang.event.onKick,
            removePreviousTgReplyMarkup = true,
//            additionalConsumer = { hasError, entity ->
//                if (!hasError) try {
//                    bot.banChatMember(msg.chat.id, entity!!.userId)
//                } catch (_: Exception) {}
//            },
            removeProtectedContent = true,
        )
    suspend fun onTelegramRestrictCommand(message: TgMessage): Boolean {
        val entity = SQLUser.getByTempArray(message.replyToMessage?.messageId.toString())
            ?: RequestsLogic.matchEntityFromUpdateServerPlayerStatusCommand(message, false)
        val errorDueExecuting = RequestsLogic.executeCheckPermissionsAndExceptions(
            message = message,
            entity = entity,
            allowedExecutionAccountTypes = listOf(AccountType.ADMIN),
            allowedExecutionIfSpendByItself = false,
            applyAccountStatuses = MinecraftAccountType.getAllMaybeActive(),
            targetAccountStatus = MinecraftAccountType.BANNED,
            targetAccountType = AccountType.UNKNOWN,
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
            entity!!.deleteProtected(AccountType.UNKNOWN)
            val requests = entity.data.getCasted(RequestsChatDataType)?:listOf()
            try {
                requests.filter { it.request_status == RequestType.ACCEPTED } .forEach {
                    bot.editMessageReplyMarkup(
                        chatId = entity.userId,
                        messageId = it.message_id_in_chat_with_user.toInt(),
                        replyMarkup = TgReplyMarkup()
                    )
                }
            } catch (_: Exception) {}
            entity.data.set(RequestsChatDataType, requests.filter { it1 -> it1.request_status != RequestType.CREATING })
            requests.firstOrNull { RequestType.getAllPending().contains(it.request_status) } ?.let {
                entity.editRequest(it.apply { this.request_status = RequestType.REJECTED })
            }
            if (newMessage!=null)
                requests.filter { it.request_status == RequestType.ACCEPTED } .forEach {
                    entity.editRequest(it.apply { this.message_id_in_chat_with_user = newMessage.messageId.toLong() })
                }
//            try {
//                bot.banChatMember(message.chat.id, entity.userId)
//            } catch (_: Exception) {}
            entity.isRestricted = true
        }
        return errorDueExecuting
    }
    suspend fun onTelegramStartCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        val entity = SQLUser.getOrCreate(msg.from?.id?:return false)
        if (entity.isRestricted) return false
        bot.sendMessage(
            chatId = msg.chat.id,
            text = config.user.lang.event.onStart,
            replyMarkup = TgMenu(listOf(listOf(
                SQLCallback.of(
                    display = config.user.lang.button.createRequest,
                    type = "requests",
                    data = RequestsBotUpdateManager.RequestCallback(RequestsBotUpdateManager.Operations.CREATE_REQUEST),
                )
            )))
        )
        return true
    }
    suspend fun onTelegramNewCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        if (msg.from == null) return false
        val entity = SQLUser.get(msg.from.id)?:return false
        if (entity.isRestricted) return false
        return RequestsLogic.newRequest(entity)
    }
    suspend fun onTelegramCancelCommand(msg: TgMessage): Boolean {
        if (msg.chat.id < 0) return true
        val entity = SQLUser.get(msg.from?.id?:return false)?:return false
        val requests = entity.data.getCasted(RequestsChatDataType)?:listOf()
        if (requests.any { RequestType.getAllPending().contains(it.request_status)}) return RequestsLogic.cancelRequest(
            entity
        )
        else if (requests.any {it.request_status == RequestType.CREATING}) return RequestsLogic.cancelSendingRequest(
            entity
        )
        return false
    }
}