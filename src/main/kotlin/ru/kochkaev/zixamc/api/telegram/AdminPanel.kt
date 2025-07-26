package ru.kochkaev.zixamc.api.telegram

import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLChat
import ru.kochkaev.zixamc.api.sql.SQLProcess
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.callback.CallbackData
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.process.ProcessTypes
import ru.kochkaev.zixamc.api.telegram.model.ITgMenuButton
import ru.kochkaev.zixamc.api.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters

object AdminPanel {

    val BACK_BUTTON = SQLCallback.of(
        display = ServerBot.config.adminPanel.buttonBackToPanel,
        type = "admin",
        data = AdminPanelCallback.of("back")
    )
    val BACK_BUTTON_NEW_MESSAGE = SQLCallback.of(
        display = ServerBot.config.adminPanel.buttonBackToPanel,
        type = "admin",
        data = AdminPanelCallback.of($$"back$newMessage")
    )

    private val integrations = arrayListOf<Integration>()
    fun addIntegration(integration: Integration, additionalType: Class<*> = AdminPanelCallback.DummyAdditional::class.java) {
        integrations.add(integration)
        val type = additionalType.name
        if (!integrationTypes.contains(type))
            integrationTypes[type] = additionalType
    }
    data class Integration(
        val button: List<ITgMenuButton>,
        val callbackProcessor: suspend (TgCallbackQuery, SQLCallback<AdminPanelCallback<*>>) -> TgCBHandlerResult,
        val filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
    ) {
        companion object {
            fun of(
                button: ITgMenuButton,
                filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
            ): Integration = Integration(listOf(button), { _, _ -> TgCBHandlerResult.SUCCESS }, filter)
            fun of(
                callbackName: String,
                display: String,
                processor: suspend (TgCallbackQuery, SQLCallback<AdminPanelCallback<AdminPanelCallback.DummyAdditional>>) -> TgCBHandlerResult,
                filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
            ): Integration = of(callbackName, display, processor, AdminPanelCallback.DummyAdditional::class.java, AdminPanelCallback.DummyAdditional(), filter)
            fun <T> of(
                callbackName: String,
                display: String,
                processor: suspend (TgCallbackQuery, SQLCallback<AdminPanelCallback<T>>) -> TgCBHandlerResult,
                customDataType: Class<T>,
                customDataInitial: T,
                filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
            ): Integration {
                return Integration(
                    button = listOf(
                        SQLCallback.of(
                        display = display,
                        type = "admin",
                        data = AdminPanelCallback.of(callbackName, customDataType, customDataInitial),
                    )),
                    callbackProcessor = { cbq, sql ->
                        if (sql.data?.operation == callbackName)
                            @Suppress("UNCHECKED_CAST")
                            processor(cbq, sql as SQLCallback<AdminPanelCallback<T>>)
                        else TgCBHandlerResult.SUCCESS
                    },
                    filter = filter,
                )
            }
        }
    }

    suspend fun sendPanel(chatId: Long, userId: Long?, messageId: Int? = null, newMessage: Boolean = false): Boolean {
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
        if (user != null && chat != null && user.hasProtectedLevel(AccountType.ADMIN) && chat.hasProtectedLevel(AccountType.ADMIN)) {
            val text = ServerBot.config.adminPanel.messagePanel.formatLang("nickname" to (user.nickname ?: ""))
            val replyMarkup = TgMenu(arrayListOf<List<ITgMenuButton>>().apply {
                addAll(integrations.filter { it.filter(chatId, userId) }.map { it.button })
                add(listOf(SQLCallback.of(
                    display = ConfigManager.config.general.buttons.success,
                    type = "admin",
                    data = AdminPanelCallback.of("success")
                )))
            })
            if (newMessage || messageId == null) ServerBot.bot.sendMessage(
                chatId = chatId,
                text = text,
                replyMarkup = replyMarkup,
                replyParameters = messageId?.let { TgReplyParameters(it) },
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
            return true
        }
        else {
            ServerBot.bot.sendMessage(
                chatId = chatId,
                text = ServerBot.config.adminPanel.messageNotAdmin,
                replyParameters = messageId?.let { TgReplyParameters(it) },
            )
            return false
        }
    }
    suspend fun onCallback(cbq: TgCallbackQuery, sql: SQLCallback<AdminPanelCallback<*>>): TgCBHandlerResult {
        val user = SQLUser.get(cbq.from.id)
        val chat = SQLChat.get(cbq.message.chat.id)
        if (user == null || chat == null || !user.hasProtectedLevel(AccountType.ADMIN) || !chat.hasProtectedLevel(AccountType.ADMIN)) {
            ServerBot.bot.editMessageText(
                chatId = cbq.message.chat.id,
                messageId = cbq.message.messageId,
                text = ServerBot.config.adminPanel.messageNotAdmin,
            )
            return TgCBHandlerResult.DELETE_MARKUP
        }
        when (sql.data!!.operation) {
            "back" -> {
                sendPanel(cbq.message.chat.id, cbq.from.id, cbq.message.messageId, false)
                return TgCBHandlerResult.DELETE_LINKED
            }
            $$"back$newMessage" -> {
                sendPanel(cbq.message.chat.id, cbq.from.id, cbq.message.messageId, true)
                return TgCBHandlerResult.DELETE_MARKUP
            }
            "success" -> {
                return TgCBHandlerResult.DELETE_MESSAGE
            }
            else -> {
                integrations.map { it.callbackProcessor }.forEach { it(cbq, sql).let { it1 -> if (it1 != TgCBHandlerResult.SUCCESS) return it1 } }
            }
        }
        return TgCBHandlerResult.DELETE_MARKUP
    }

    val integrationTypes = hashMapOf<String, Class<*>>()
    class AdminPanelCallback <T> private constructor(
        val operation: String,
        val additionalType: String,
        val additional: T,
    ) : CallbackData {
        open class DummyAdditional()
        companion object {
            fun of(operation: String): AdminPanelCallback<DummyAdditional> =
                AdminPanelCallback(operation, "dummy", DummyAdditional())
            fun <T> of(operation: String, additionalType: Class<T>, additional: T): AdminPanelCallback<T> {
                val type = additionalType.name
                if (!integrationTypes.contains(type))
                    integrationTypes[type] = additionalType
                return AdminPanelCallback(operation, type, additional)
            }
        }
    }
}