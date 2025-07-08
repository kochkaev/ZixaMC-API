package ru.kochkaev.zixamc.api.telegram

import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLChat
import ru.kochkaev.zixamc.api.sql.SQLProcess
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.callback.CallbackCanExecute
import ru.kochkaev.zixamc.api.sql.callback.CallbackData
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.process.ProcessTypes
import ru.kochkaev.zixamc.api.telegram.RulesManager.RulesCallbackData
import ru.kochkaev.zixamc.api.telegram.RulesManager.RulesOperation
import ru.kochkaev.zixamc.api.telegram.model.ITgMenuButton
import ru.kochkaev.zixamc.api.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.api.telegram.model.TgChatMemberStatuses
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters

object Menu {

    val BACK_BUTTON = SQLCallback.of(
        display = ServerBot.config.menu.buttonBackToMenu,
        type = "menu",
        data = MenuCallbackData.of("back")
    )
    val BACK_BUTTON_NEW_MESSAGE = SQLCallback.of(
        display = ServerBot.config.menu.buttonBackToMenu,
        type = "menu",
        data = MenuCallbackData.of($$"back$newMessage")
    )
    fun getBackButtonExecuteOnly(user: SQLUser, newMessage: Boolean = false) = SQLCallback.of(
        display = ServerBot.config.menu.buttonBackToMenu,
        type = "menu",
        data = MenuCallbackData.of(if (newMessage) $$"back$newMessage" else "back"),
        canExecute = CallbackCanExecute(
            statuses = listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR),
            users = listOf(user.userId),
            display = user.nickname ?: "",
        )
    )

    private val integrations = arrayListOf<Integration>()
    fun addIntegration(integration: Integration, additionalType: Class<*> = MenuCallbackData.DummyAdditional::class.java) {
        integrations.add(integration)
        val type = additionalType.name
        if (!integrationTypes.contains(type))
            integrationTypes[type] = additionalType
    }
    data class Integration(
        val menuButton: List<ITgMenuButton>,
        val menuCallbackProcessor: suspend (TgCallbackQuery, SQLCallback<MenuCallbackData<*>>) -> TgCBHandlerResult,
        val filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
    ) {
        companion object {
            fun of(
                menuButton: ITgMenuButton,
                filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
            ): Integration = Integration(listOf(menuButton), { _, _ -> TgCBHandlerResult.SUCCESS }, filter)
            fun of(
                callbackName: String,
                menuDisplay: String,
                processor: suspend (TgCallbackQuery, SQLCallback<MenuCallbackData<MenuCallbackData.DummyAdditional>>) -> TgCBHandlerResult,
                filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
            ): Integration = of(callbackName, menuDisplay, processor, MenuCallbackData.DummyAdditional::class.java, MenuCallbackData.DummyAdditional(), filter)
            fun <T> of(
                callbackName: String,
                menuDisplay: String,
                processor: suspend (TgCallbackQuery, SQLCallback<MenuCallbackData<T>>) -> TgCBHandlerResult,
                customDataType: Class<T>,
                customDataInitial: T,
                filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
            ): Integration {
                return Integration(
                    menuButton = listOf(
                        SQLCallback.of(
                        display = menuDisplay,
                        type = "menu",
                        data = MenuCallbackData.of(callbackName, customDataType, customDataInitial),
                    )),
                    menuCallbackProcessor = { cbq, sql ->
                        if (sql.data?.operation == callbackName)
                            processor(cbq, sql as SQLCallback<MenuCallbackData<T>>)
                        else TgCBHandlerResult.SUCCESS
                    },
                    filter = filter,
                )
            }
        }
    }

    suspend fun sendMenu(chatId: Long, userId: Long?, replyTo: Int? = null, newMessage: Boolean = false, messageId: Int? = null) {
        val chat = SQLChat.get(chatId)
        val user = userId?.let { SQLUser.get(it) }
        ProcessTypes.entries.values
            .filter { it.cancelOnMenuSend }
            .mapNotNull { SQLProcess.get(chatId, it) }
            .forEach { process ->
                process.data?.run {
                    try {
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = process.chatId,
                            messageId = this.messageId,
                            replyMarkup = TgReplyMarkup()
                        )
                    } catch (_: Exception) {
                    }
                    SQLCallback.dropAll(process.chatId, this.messageId)
                }
                process.drop()
            }
        if (user != null && chat != null && user.hasProtectedLevel(AccountType.PLAYER)) {
            val text = ServerBot.config.menu.messageMenu.formatLang("nickname" to (user.nickname ?: ""))
            val replyMarkup = TgMenu(arrayListOf<List<ITgMenuButton>>().apply {
                addAll(integrations.filter { it.filter(chatId, userId) }.map { it.menuButton })
                if (chatId == userId && SQLUser.get(userId)?.agreedWithRules == true) add(listOf(SQLCallback.of(
                    display = ServerBot.config.menu.removeAgreedWithRules,
                    type = "menu",
                    data = MenuCallbackData.of("removeAgreedWithRules")
                )))
                add(listOf(SQLCallback.of(
                    display = ConfigManager.config.general.buttons.success,
                    type = "menu",
                    data = MenuCallbackData.of("success")
                )))
            })
            if (newMessage || messageId == null) ServerBot.bot.sendMessage(
                chatId = chatId,
                text = text,
                replyMarkup = replyMarkup,
                replyParameters = replyTo?.let { TgReplyParameters(it) },
            )
            else {
                ServerBot.bot.editMessageText(
                    chatId = chatId,
                    messageId = messageId,
                    text = text,
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = chatId,
                    messageId = messageId,
                    replyMarkup = replyMarkup,
                )
            }
        }
        else
            ServerBot.bot.sendMessage(
                chatId = chatId,
                text = ServerBot.config.menu.messageNotPlayer,
                replyParameters = replyTo?.let { TgReplyParameters(it) },
            )
    }
    suspend fun onCallback(cbq: TgCallbackQuery, sql: SQLCallback<MenuCallbackData<*>>): TgCBHandlerResult {
//        if (cbq.data == null || !cbq.data.startsWith("menu")) return
        val entity = SQLUser.get(cbq.from.id)?:return TgCBHandlerResult.SUCCESS
        if (!entity.hasProtectedLevel(AccountType.PLAYER)) {
            ServerBot.bot.editMessageText(
                chatId = cbq.message.chat.id,
                messageId = cbq.message.messageId,
                text = ServerBot.config.menu.messageNotPlayer,
            )
            return TgCBHandlerResult.DELETE_MARKUP
        }
        when (sql.data!!.operation /*cbq.data*/) {
            "back" -> {
                sendMenu(cbq.message.chat.id, cbq.from.id, null, false, cbq.message.messageId)
                return TgCBHandlerResult.DELETE_LINKED
            }
            $$"back$newMessage" -> {
                sendMenu(cbq.message.chat.id, cbq.from.id, cbq.message.messageId, true, null)
                return TgCBHandlerResult.DELETE_MARKUP
            }
//            "info" -> {
//                ServerBotLogic.sendInfoMessage(entity)
//                return TgCBHandlerResult.DELETE_MESSAGE
//            }
            "removeAgreedWithRules" -> {
                ServerBot.bot.editMessageText(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    text = ConfigManager.config.general.rules.confirmRemoveAgree4player.formatLang("nickname" to (SQLUser.get(cbq.from.id)?.nickname?:""))
                )
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = cbq.message.chat.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(listOf(
                        listOf(SQLCallback.of(
                            display = ConfigManager.config.general.buttons.confirm,
                            type = "rules",
                            data = RulesCallbackData(RulesOperation.CONFIRM_REMOVE_AGREE, RulesManager.RulesOperationType.REMOVE_AGREE)
                        )),
                        listOf(SQLCallback.of(
                            display = ConfigManager.config.general.buttons.cancel,
                            type = "rules",
                            data = RulesCallbackData(RulesOperation.CANCEL_REMOVE_AGREE, RulesManager.RulesOperationType.REMOVE_AGREE, cbq.from.id)
                        )),
                    ))
                )
                return TgCBHandlerResult.DELETE_LINKED
            }
            "success" -> {
                return TgCBHandlerResult.DELETE_MESSAGE
            }
            else -> {
                integrations.map { it.menuCallbackProcessor }.forEach { it(cbq, sql).let { it1 -> if (it1 != TgCBHandlerResult.SUCCESS) return it1 } }
            }
        }
        return TgCBHandlerResult.DELETE_MARKUP
    }
//    suspend fun onMessage(msg: TgMessage) {
//        runBlocking {
//            val chat = SQLChat.get(msg.chat.id)
//            if (chat != null && chat.hasProtectedLevel(AccountType.PLAYER))
//                SQLProcess.get(chat.id, ProcessTypes.MENU_AUDIO_PLAYER_UPLOAD)?.also {
//
//        }
//    }

    val integrationTypes = hashMapOf<String, Class<*>>()
    open class MenuCallbackData <T> private constructor(
        val operation: String,
        val additionalType: String,
        val additional: T,
    ) : CallbackData {
        open class DummyAdditional()
        companion object {
            fun of(operation: String): MenuCallbackData<DummyAdditional> =
                MenuCallbackData(operation, "dummy", DummyAdditional())
            fun <T> of(operation: String, additionalType: Class<T>, additional: T): MenuCallbackData<T> {
                val type = additionalType.name
                if (!integrationTypes.contains(type))
                    integrationTypes[type] = additionalType
                return MenuCallbackData(operation, type, additional)
            }
        }
    }
}