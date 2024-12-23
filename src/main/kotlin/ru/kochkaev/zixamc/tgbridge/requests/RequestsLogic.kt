package ru.kochkaev.zixamc.tgbridge.requests

import ru.kochkaev.zixamc.tgbridge.MySQLIntegration
import ru.kochkaev.zixamc.tgbridge.SQLEntity
import ru.kochkaev.zixamc.tgbridge.RequestsBot.bot
import ru.kochkaev.zixamc.tgbridge.RequestsBot.config
import ru.kochkaev.zixamc.tgbridge.BotLogic
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.ProtectedMessageData
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.RequestData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.*

object RequestsLogic {

    suspend fun cancelRequest(entity: SQLEntity): Boolean {
        val request = (entity.data?:return false).requests.firstOrNull { it.request_status == "pending" } ?: return false
        entity.editRequest(entity.data!!.requests.first { it.request_status == "pending" }.apply { this.request_status = "canceled" })
        bot.sendMessage(
            chatId = entity.userId,
            text = BotLogic.escapePlaceholders(config.text.events.forUser.textRequestCanceled4User, entity.nickname),
            replyMarkup = TgInlineKeyboardMarkup(
                inline_keyboard = listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.buttons.textButtonCreateRequest,
                    callback_data = "create_request",
                )))
            )
        )
        bot.sendMessage(
            chatId = config.targetChatId,
            messageThreadId = config.targetTopicId,
            text = BotLogic.escapePlaceholders(config.text.events.forTarget.textRequestCanceled4Target, entity.nickname),
            replyParameters = TgReplyParameters(
                message_id = request.message_id_in_target_chat!!.toInt()
            )
        )
        entity.tempArray = arrayOf()
        return true
    }
    suspend fun cancelSendingRequest(entity: SQLEntity): Boolean {
        entity.data = entity.data.apply {
            (this ?: return false).requests.filter { it.request_status == "creating" }
        }
        bot.sendMessage(
            chatId = entity.userId,
            text = config.text.events.forUser.textRequestCanceled4User,
            replyMarkup = TgInlineKeyboardMarkup(
                inline_keyboard = listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = BotLogic.escapePlaceholders(config.text.buttons.textButtonCreateRequest),
                    callback_data = "create_request",
                )))
            )
        )
        return true
    }
    suspend fun newRequest(entity: SQLEntity): Boolean {
        when (entity.createAndOrGetData().requests.firstOrNull { listOf("creating", "pending").contains(it.request_status) }?.request_status ?: "") {
            "creating" -> {
                bot.sendMessage(
                    chatId = entity.userId,
                    text = BotLogic.escapePlaceholders(config.text.messages.textYouAreNowCreatingRequest),
                    replyMarkup = TgInlineKeyboardMarkup(
                        inline_keyboard = listOf(listOf(
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.text.buttons.textButtonRedrawRequest,
                            callback_data = "redraw_request",
                        )))
                    )
                )
                return false
            }
            "pending" -> {
                bot.sendMessage(
                    chatId = entity.userId,
                    text = BotLogic.escapePlaceholders(config.text.messages.textYouHavePendingRequest, entity.nickname),
                    replyMarkup = TgInlineKeyboardMarkup(
                        inline_keyboard = listOf(listOf(
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = config.text.buttons.textButtonCancelRequest,
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
                text = BotLogic.escapePlaceholders(config.text.messages.textYouAreNowPlayer, entity.nickname),
            )
            return false
        }
        val forReplyMessage = if (entity.agreedWithRules) bot.sendMessage(
            chatId = entity.userId,
            text = config.text.messages.textNeedNickname,
            replyMarkup = TgForceReply(
                true,
                config.text.inputFields.textInputFieldPlaceholderNickname.ifEmpty { null }
            )
        )
        else bot.sendMessage(
            chatId = entity.userId,
            text = BotLogic.escapePlaceholders(config.text.messages.textNeedAgreeWithRules),
            replyMarkup = TgInlineKeyboardMarkup(
                listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.buttons.textButtonAgreeWithRules,
                    callback_data = "agree_with_rules",
                ))),
            )
        )
        MySQLIntegration.addRequest(entity.userId, RequestData(
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

    fun promoteUser(argEntity: SQLEntity? = null, userId: Long? = null, nickname: String? = null, targetName: String? = null, argTargetId: Int? = null): Boolean {
        val entity = argEntity ?:
        if (userId != null) MySQLIntegration.getLinkedEntity(userId) ?: return false
        else if (nickname != null) MySQLIntegration.getLinkedEntityByNickname(nickname) ?: return false
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
        entity: SQLEntity = MySQLIntegration.getOrRegisterLinkedEntity(message.from!!.id),
        allowedAccountTypes: List<Int> = listOf(0),
        allowedIfSpendByItself: Boolean = false,
    ): Boolean =
        !(entity.accountType in allowedAccountTypes && (!allowedIfSpendByItself || message.from!!.id==entity.userId))

    fun matchEntityFromUpdateServerPlayerStatusCommand(msg: TgMessage, allowedIfSpendByItself: Boolean = false): SQLEntity? {
        val args = msg.text!!.split(" ")
        val isArgUserId = if (args.size > 1) args[1].matches("[0-9]+".toRegex()) && args[1].length == 10 else false
        val isReplyToMessage = msg.replyToMessage != null
        val isItLegalReply = isReplyToMessage && msg.replyToMessage!!.messageId != config.targetTopicId
        val entity =
            if (isArgUserId)
                MySQLIntegration.getLinkedEntity(args[1].toLong())
            else if (isItLegalReply)
                MySQLIntegration.getLinkedEntity(msg.replyToMessage!!.from?.id ?: return null)
            else if (!isReplyToMessage && args.size>1 && args[1].matches("[a-zA-Z0-9_]+".toRegex()) && args[1].length in 3..16)
                MySQLIntegration.getLinkedEntityByNickname(args[1])
            else if (allowedIfSpendByItself)
                MySQLIntegration.getLinkedEntity(msg.from!!.id)
            else null
        return entity
    }

    fun updateServerPlayerStatus(
        entity: SQLEntity,
        applyAccountStatuses: List<String> = listOf("admin", "player", "old", "banned", "frozen"),
        targetAccountStatus: String = "player",
        targetAccountType: Int = matchAccountTypeFromMinecraftAccountStatus(targetAccountStatus),
        editWhitelist: Boolean = false
    ) : Boolean {
        val isTargetPlayer = isPlayer(targetAccountType)
        if (!promoteUser(
                argEntity = entity,
                argTargetId = targetAccountType,
            )
        ) return false
        else {
            entity.data?.minecraftAccounts
                ?.filter { applyAccountStatuses.contains(it.accountStatus) }
                ?.map { it.nickname }
                ?.forEach {
                    if (editWhitelist) ZixaMCTGBridge.let { main ->
                        try {
                            if (isTargetPlayer) main.addToWhitelist(it)
                            else main.removeFromWhitelist(it)
                        } catch (_: Exception) {}
                    }
                    entity.editMinecraftAccount(it, targetAccountStatus)
                }
            return true
        }
    }

    fun matchAccountTypeFromMinecraftAccountStatus(status: String): Int = when (status) {
        "admin" -> 0
        "player" -> 1
        "banned", "frozen" -> 2
        else -> 3
    }
    fun isPlayer(accountType: Int): Boolean = accountType<=1

    suspend fun deleteProtected(
        protected: List<ProtectedMessageData>,
        protectLevel: Int,
    ) = BotLogic.deleteProtected(
        bot = bot,
        protected = protected,
        protectLevel = protectLevel,
    )

    suspend fun sendOnJoinInfoMessage(
        entity: SQLEntity,
        replyToMessageID: Int? = null,
    ) : TgMessage? = BotLogic.sendInfoMessage(
            bot = bot,
            chatId = entity.userId,
            replyParameters = if (replyToMessageID!=null) TgReplyParameters(replyToMessageID) else null,
            replyMarkup = TgInlineKeyboardMarkup(listOf(
                listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.buttons.textButtonJoinToPlayersGroup,
                    url = config.playersGroupInviteLink,
                )),
                listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.buttons.textButtonCopyServerIP,
                    copy_text = TgInlineKeyboardMarkup.TgInlineKeyboardButton.TgCopyTextButton(config.serverIP),
                )),
            )),
            entity = entity,
        )

    suspend fun executeCheckPermissionsAndExceptions(
        message: TgMessage,
        entity: SQLEntity?,
        allowedExecutionAccountTypes: List<Int> = listOf(0),
        allowedExecutionIfSpendByItself: Boolean = false,
        applyAccountStatuses: List<String> = listOf("admin", "player", "old", "banned", "frozen"),
        targetAccountStatus: String = "player",
        targetAccountType: Int = matchAccountTypeFromMinecraftAccountStatus(targetAccountStatus),
        editWhitelist: Boolean = false,
        helpText: String? = null,
    ) : Boolean {
        var errorDueExecuting = false
        var havePermission = true
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
                targetAccountType = targetAccountType,
                editWhitelist = editWhitelist,
            )
        ) errorDueExecuting = true
        if (errorDueExecuting && helpText != null) {
            bot.sendMessage(
                chatId = message.chat.id,
                messageThreadId = message.messageThreadId,
                text =
                    if (!havePermission) BotLogic.escapePlaceholders(
                        config.text.commands.textCommandPermissionDenied,
                        entity?.nickname
                    )
                    else BotLogic.escapePlaceholders(helpText, entity?.nickname),
                replyParameters = TgReplyParameters(message.messageId),
            )
        }
        return errorDueExecuting
    }
}