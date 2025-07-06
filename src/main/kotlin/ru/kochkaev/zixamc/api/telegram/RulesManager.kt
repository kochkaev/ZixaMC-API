package ru.kochkaev.zixamc.api.telegram

import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.callback.CallbackData
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.feature.FeatureTypes
import ru.kochkaev.zixamc.api.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters

object RulesManager {

    suspend fun updateRules(
        executor: SQLUser,
        revokeAccepts: Boolean = false,
    ): Boolean {
        if (!executor.hasProtectedLevel(AccountType.ADMIN)) return false
        SQLGroup.groups.forEach { it.sendRulesUpdated(revokeAccepts) }
        SQLUser.users.filter { it.agreedWithRules } .forEach { it.sendRulesUpdated(revokeAccepts) }
        return true
    }

    data class RulesCallbackData(
        val operation: RulesOperation,
        val type: RulesOperationType,
        val id: Long? = null,
    ): CallbackData
    enum class RulesOperation {
        SET_AGREE,
        REMOVE_AGREE,
        CONFIRM_REMOVE_AGREE,
        CANCEL_REMOVE_AGREE,
        SET_AGREE_GROUP,
        REMOVE_AGREE_GROUP,
        CONFIRM_REMOVE_AGREE_GROUP,
        CANCEL_REMOVE_AGREE_GROUP,
    }
    enum class RulesOperationType {
        RULES_UPDATED,
        REMOVE_AGREE
    }

    suspend fun onCallback(cbq: TgCallbackQuery, sql: SQLCallback<RulesCallbackData>): TgCBHandlerResult {
        if (sql.data == null) return TgCBHandlerResult.SUCCESS
        val group = SQLGroup.get(cbq.message.chat.id)
        val user = SQLUser.get(cbq.from.id)?:return TgCBHandlerResult.SUCCESS
        val bot = cbq.message.from?.id?.let { BotLogic.getBot(it) } ?: return TgCBHandlerResult.SUCCESS
        when(sql.data!!.operation) {
            RulesOperation.SET_AGREE -> {
                user.agreedWithRules = true
                return if (group!=null) TgCBHandlerResult.SUCCESS else TgCBHandlerResult.DELETE_MARKUP
            }
            RulesOperation.REMOVE_AGREE -> {
                val text = ConfigManager.config.general.rules.confirmRemoveAgree4player.formatLang("nickname" to (user.nickname?:cbq.from.firstName))
                if (group!=null) {
                    bot.sendMessage(
                        chatId = cbq.message.chat.id,
                        text = text,
                        replyMarkup = TgMenu(listOf(
                            listOf(SQLCallback.of(
                                display = ConfigManager.config.general.buttons.confirm,
                                type = "rules",
                                data = RulesCallbackData(RulesOperation.CONFIRM_REMOVE_AGREE, sql.data!!.type, user.id)
                            )),
                            listOf(SQLCallback.of(
                                display = ConfigManager.config.general.buttons.cancel,
                                type = "rules",
                                data = RulesCallbackData(RulesOperation.CANCEL_REMOVE_AGREE, sql.data!!.type, user.id)
                            )),
                        )),
                        replyParameters = TgReplyParameters(cbq.message.messageId),
                    )
                    return TgCBHandlerResult.SUCCESS
                } else {
                    bot.editMessageText(
                        chatId = cbq.message.chat.id,
                        messageId = cbq.message.messageId,
                        text = text,
                    )
                    bot.editMessageReplyMarkup(
                        chatId = cbq.message.chat.id,
                        messageId = cbq.message.messageId,
                        replyMarkup = TgMenu(listOf(
                            listOf(SQLCallback.of(
                                display = ConfigManager.config.general.buttons.confirm,
                                type = "rules",
                                data = RulesCallbackData(RulesOperation.CONFIRM_REMOVE_AGREE, sql.data!!.type)
                            )),
                            listOf(SQLCallback.of(
                                display = ConfigManager.config.general.buttons.cancel,
                                type = "rules",
                                data = RulesCallbackData(RulesOperation.CANCEL_REMOVE_AGREE, sql.data!!.type)
                            )),
                        )),
                    )
                    return TgCBHandlerResult.DELETE_LINKED
                }
            }
            RulesOperation.CONFIRM_REMOVE_AGREE -> {
                user.agreedWithRules = false
                if (user.hasProtectedLevel(AccountType.PLAYER)) {
                    SQLGroup.getAllWithFeature(FeatureTypes.PLAYERS_GROUP).filter { it.members.contains(user.id) } .forEach { chat ->
                        for (it in BotLogic.bots) try {
                            it.banChatMember(chat.id, user.id)
                            it.sendMessage(
                                chatId = cbq.message.chat.id,
                                text = ConfigManager.config.general.rules.onLeave4group
                            )
                        } catch (_: Exception) {}
                    }
                    user.accountType = AccountType.REQUESTER
                    user.deleteProtected(AccountType.PLAYER)
                    if (group!=null) for (it in BotLogic.bots) try {
                        it.sendMessage(
                            chatId = user.id,
                            text = ConfigManager.config.general.rules.onLeave4player
                        )
                        break
                    } catch (_: Exception) {}
                    else bot.sendMessage(
                        chatId = user.id,
                        text = ConfigManager.config.general.rules.onLeave4player
                    )
                }
                return if (group!=null) TgCBHandlerResult.DELETE_MESSAGE else TgCBHandlerResult.DELETE_LINKED
            }
            RulesOperation.CANCEL_REMOVE_AGREE -> {
                if (user.id != sql.data!!.id) {
                    bot.answerCallbackQuery(
                        callbackQueryId = cbq.id,
                        text = ConfigManager.config.general.rules.thatButtonFor.formatLang(
                            "nickname" to (sql.data!!.id?.let { SQLUser.get(it)?.nickname ?: it.toString() } ?: "")
                        ),
                        showAlert = true,
                    )
                    return TgCBHandlerResult.SUCCESS
                }
                if (group!=null) {
                    return TgCBHandlerResult.DELETE_MESSAGE
                } else {
                    when(sql.data!!.type) {
                        RulesOperationType.RULES_UPDATED -> {
                            bot.editMessageText(
                                chatId = cbq.message.chat.id,
                                messageId = cbq.message.messageId,
                                text = ConfigManager.config.general.rules.updated4player,
                            )
                            bot.editMessageReplyMarkup(
                                chatId = cbq.message.chat.id,
                                messageId = cbq.message.messageId,
                                replyMarkup = TgMenu(listOf(
                                    listOf(SQLCallback.of(
                                        display = ConfigManager.config.general.rules.removeButton,
                                        type = "rules",
                                        data = RulesCallbackData(RulesOperation.REMOVE_AGREE, sql.data!!.type)
                                    )),
                                )),
                            )
                            return TgCBHandlerResult.DELETE_LINKED
                        }
                        RulesOperationType.REMOVE_AGREE -> {
                            Menu.sendMenu(user.id, user.id)
                            return TgCBHandlerResult.DELETE_MESSAGE
                        }
                    }
                }
            }
            RulesOperation.SET_AGREE_GROUP -> {
                if (group==null) return TgCBHandlerResult.SUCCESS
                group.agreedWithRules = true
                return TgCBHandlerResult.DELETE_MARKUP
            }
            RulesOperation.REMOVE_AGREE_GROUP -> {
                if (group==null) return TgCBHandlerResult.SUCCESS
                bot.editMessageText(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    text = ConfigManager.config.general.rules.confirmRemoveAgree4group,
                )
                bot.editMessageReplyMarkup(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(listOf(
                        listOf(SQLCallback.of(
                            display = ConfigManager.config.general.buttons.confirm,
                            type = "rules",
                            data = RulesCallbackData(RulesOperation.CONFIRM_REMOVE_AGREE_GROUP, sql.data!!.type)
                        )),
                        listOf(SQLCallback.of(
                            display = ConfigManager.config.general.buttons.cancel,
                            type = "rules",
                            data = RulesCallbackData(RulesOperation.CANCEL_REMOVE_AGREE_GROUP, sql.data!!.type)
                        )),
                    )),
                )
                return TgCBHandlerResult.DELETE_LINKED
            }
            RulesOperation.CONFIRM_REMOVE_AGREE_GROUP -> {
                if (group==null) return TgCBHandlerResult.SUCCESS
                bot.sendMessage(
                    chatId = group.chatId,
                    text = ServerBot.config.group.wait,
                    replyParameters = TgReplyParameters(cbq.message.messageId)
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgReplyMarkup()
                )
                val sanitized = arrayListOf<Int>()
                SQLCallback.getAll(group.chatId).sortedByDescending { it.messageId } .forEach {
                    it.messageId?.also { messageId -> if (!sanitized.contains(messageId)) try {
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = group.chatId,
                            messageId = messageId,
                            replyMarkup = TgReplyMarkup()
                        )
                        sanitized.add(messageId)
                    } catch (_: Exception) { } }
                    it.drop()
                }
                try {
                    group.deleteProtected(AccountType.UNKNOWN)
                } catch (_: Exception) {}
                group.members.set(listOf())
                group.features.setAll(mapOf())
                group.agreedWithRules = false
                BotLogic.bots.forEach {
                    try {
                        it.leaveChat(group.chatId)
                    } catch (_: Exception) {}
                }
                return TgCBHandlerResult.DELETE_MARKUP
            }
            RulesOperation.CANCEL_REMOVE_AGREE_GROUP -> {
                if (group==null) return TgCBHandlerResult.SUCCESS
                when (sql.data!!.type) {
                    RulesOperationType.RULES_UPDATED -> {
                        bot.editMessageText(
                            chatId = cbq.message.chat.id,
                            messageId = cbq.message.messageId,
                            text = ConfigManager.config.general.rules.updated4group
                        )
                        bot.editMessageReplyMarkup(
                            chatId = cbq.message.chat.id,
                            messageId = cbq.message.messageId,
                            replyMarkup = TgMenu(listOf(
                                listOf(SQLCallback.of(
                                    display = ConfigManager.config.general.rules.removeButton,
                                    type = "rules",
                                    data = RulesCallbackData(RulesOperation.REMOVE_AGREE_GROUP, sql.data!!.type)
                                )),
                            )),
                        )
                        return TgCBHandlerResult.DELETE_LINKED
                    }
                    RulesOperationType.REMOVE_AGREE -> {
                        bot.editMessageText(
                            chatId = cbq.message.chat.id,
                            messageId = cbq.message.messageId,
                            text = ServerBot.config.group.settings.text
                        )
                        bot.editMessageReplyMarkup(
                            chatId = cbq.message.chat.id,
                            messageId = cbq.message.messageId,
                            replyMarkup = ServerBotGroup.getSettings(group),
                        )
                        return TgCBHandlerResult.DELETE_LINKED
                    }
                }

            }
        }
    }
}