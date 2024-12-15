package ru.kochkaev.zixamc.tgbridge.requests

import ru.kochkaev.zixamc.tgbridge.NewMySQLIntegration
import ru.kochkaev.zixamc.tgbridge.NewSQLEntity
import ru.kochkaev.zixamc.tgbridge.RequestsBot.bot
import ru.kochkaev.zixamc.tgbridge.RequestsBot.config
import ru.kochkaev.zixamc.tgbridge.TelegramBotZixa
import ru.kochkaev.zixamc.tgbridge.BotLogic
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.ProtectedMessageData
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.RequestData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.*

object RequestsLogic {

    suspend fun cancelRequest(entity: NewSQLEntity): Boolean {
        entity.editRequest((entity.data?:return false).requests.first { it.request_status == "pending" }.apply { this.request_status = "canceled" })
        bot.sendMessage(
            chatId = entity.userId,
            text = config.text.events.forUser.textRequestCanceled4User,
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
            text = config.text.events.forTarget.textRequestCanceled4Target,
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
            text = config.text.events.forUser.textRequestCanceled4User,
            replyMarkup = TgInlineKeyboardMarkup(
                inline_keyboard = listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.buttons.textButtonCreateRequest,
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
                    text = config.text.messages.textYouAreNowCreatingRequest,
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
                    text = config.text.messages.textYouHavePendingRequest,
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
                text = config.text.messages.textYouAreNowPlayer,
            )
            return false
        }
        val forReplyMessage = if (NewMySQLIntegration.isAgreedWithRules(entity.userId)) bot.sendMessage(
            chatId = entity.userId,
            text = config.text.messages.textNeedNickname,
            replyMarkup = TgForceReply(
                true,
                config.text.inputFields.textInputFieldPlaceholderNickname.ifEmpty { null }
            )
        )
        else bot.sendMessage(
            chatId = entity.userId,
            text = config.text.messages.textNeedAgreeWithRules,
            replyMarkup = TgInlineKeyboardMarkup(
                listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = config.text.buttons.textButtonAgreeWithRules,
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
        entity: NewSQLEntity,
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
}