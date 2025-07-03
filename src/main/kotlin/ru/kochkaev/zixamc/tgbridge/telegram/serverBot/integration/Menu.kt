package ru.kochkaev.zixamc.tgbridge.telegram.serverBot.integration

import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult.Companion.DELETE_MARKUP
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult.Companion.SUCCESS
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.ServerBotLogic
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLChat
import ru.kochkaev.zixamc.tgbridge.sql.SQLUser
import ru.kochkaev.zixamc.tgbridge.sql.SQLProcess
import ru.kochkaev.zixamc.tgbridge.sql.callback.CallbackCanExecute
import ru.kochkaev.zixamc.tgbridge.sql.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgMenu
import ru.kochkaev.zixamc.tgbridge.sql.data.AccountType
import ru.kochkaev.zixamc.tgbridge.sql.process.ProcessTypes
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.answerHaventRights
import ru.kochkaev.zixamc.tgbridge.telegram.model.ITgMenuButton
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMemberStatuses
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyMarkup

object Menu {

    val BACK_BUTTON = SQLCallback.of(
        display = ServerBot.config.integration.buttonBackToMenu,
        type = "menu",
        data = MenuCallbackData.of("back")
    )
    fun getBackButtonExecuteOnly(user: SQLUser) = SQLCallback.of(
        display = ServerBot.config.integration.buttonBackToMenu,
        type = "menu",
        data = MenuCallbackData.of("back"),
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
            ): Integration = Integration(listOf(menuButton), { _, _ -> SUCCESS }, filter)
            fun of(
                callbackName: String,
                menuDisplay: String,
                processor: suspend (TgCallbackQuery, SQLCallback<MenuCallbackData<*>>) -> Unit,
                filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
            ): Integration = of(callbackName, menuDisplay, processor as suspend (TgCallbackQuery, SQLCallback<MenuCallbackData<MenuCallbackData.DummyAdditional>>) -> TgCBHandlerResult, MenuCallbackData.DummyAdditional::class.java, MenuCallbackData.DummyAdditional(), filter)
            fun <T> of(
                callbackName: String,
                menuDisplay: String,
                processor: suspend (TgCallbackQuery, SQLCallback<MenuCallbackData<T>>) -> TgCBHandlerResult,
                customDataType: Class<T>,
                customDataInitial: T,
                filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
            ): Integration {
                return Integration(
                    menuButton = listOf(SQLCallback.of(
                        display = menuDisplay,
                        type = "menu",
                        data = MenuCallbackData.of(callbackName, customDataType, customDataInitial),
                    )),
                    menuCallbackProcessor = { cbq, sql ->
                        if (sql.data?.operation == callbackName)
                            processor(cbq, sql as SQLCallback<MenuCallbackData<T>>)
                        else SUCCESS
                    },
                    filter = filter,
                )
            }
        }
    }

    suspend fun sendMenu(chatId: Long, userId: Long?, threadId: Int? = null) {
        val chat = SQLChat.get(chatId)
        val user = userId?.let { SQLUser.get(it) }
        listOfNotNull(
            SQLProcess.get(chatId, ProcessTypes.MENU_AUDIO_PLAYER_UPLOAD),
            SQLProcess.get(chatId, ProcessTypes.MENU_FABRIC_TAILOR_UPLOAD),
        ).forEach { process ->
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
            ServerBot.bot.sendMessage(
                chatId = chatId,
                messageThreadId = threadId,
                text = ServerBot.config.integration.messageMenu,
                replyMarkup = TgMenu(arrayListOf<List<ITgMenuButton>>().apply {
                    addAll(integrations.filter { it.filter(chatId, userId) } .map { it.menuButton })
                })
            )
//            process.remove(chat.getChatId())
        }
        else
            ServerBot.bot.sendMessage(
                chatId = chatId,
                text = ServerBot.config.integration.messageNotPlayer,
            )
    }
    suspend fun onCallback(cbq: TgCallbackQuery, sql: SQLCallback<MenuCallbackData<*>>): TgCBHandlerResult {
//        if (cbq.data == null || !cbq.data.startsWith("menu")) return
        val entity = SQLUser.get(cbq.from.id)?:return SUCCESS
        if (!entity.hasProtectedLevel(AccountType.PLAYER)) {
            ServerBot.bot.editMessageText(
                chatId = cbq.message.chat.id,
                messageId = cbq.message.messageId,
                text = ServerBot.config.integration.messageNotPlayer,
            )
            return DELETE_MARKUP
        }
        if (cbq.message.chat.id != cbq.from.id &&  sql.canExecute?.let {
                !(it.statuses?.contains(ServerBot.bot.getChatMember(cbq.message.chat.id, cbq.from.id).status) == true || it.users?.contains(cbq.from.id) == true)
            } != false) return answerHaventRights(cbq.id, sql.canExecute?.display?:"")
        when (sql.data!!.operation /*cbq.data*/) {
            "back" -> sendMenu(cbq.message.chat.id, cbq.from.id, cbq.message.messageThreadId)
            "info" -> ServerBotLogic.sendInfoMessage(entity)
            else -> {
                integrations.map { it.menuCallbackProcessor }.forEach { it(cbq, sql).let { it1 -> if (it1 != SUCCESS) return it1 } }
            }
        }
        return DELETE_MARKUP
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