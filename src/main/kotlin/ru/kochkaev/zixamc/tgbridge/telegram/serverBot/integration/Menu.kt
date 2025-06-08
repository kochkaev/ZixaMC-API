package ru.kochkaev.zixamc.tgbridge.telegram.serverBot.integration

import kotlinx.coroutines.runBlocking
import net.fabricmc.loader.api.FabricLoader
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgMessage
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult.Companion.DELETE_MARKUP
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult.Companion.SUCCESS
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.ServerBotLogic
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLChat
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLProcess
import ru.kochkaev.zixamc.tgbridge.sql.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgMenu
import ru.kochkaev.zixamc.tgbridge.sql.data.AccountType
import ru.kochkaev.zixamc.tgbridge.sql.process.GroupChatSyncWaitPrefixProcessData
import ru.kochkaev.zixamc.tgbridge.sql.process.ProcessData
import ru.kochkaev.zixamc.tgbridge.sql.process.ProcessTypes
import ru.kochkaev.zixamc.tgbridge.telegram.model.ITgMenuButton
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup

object Menu {

    val BACK_BUTTON = SQLCallback.of(
        display = ServerBot.config.integration.buttonBackToMenu,
        type = "menu",
        data = MenuCallbackData.of("back")
    )

    private val integrations = arrayListOf<Integration>()
    fun addIntegration(integration: Integration) {
        integrations.add(integration)
    }
    data class Integration(
        val menuButton: List<ITgMenuButton>,
        val menuCallbackProcessor: suspend (TgCallbackQuery, SQLCallback<MenuCallbackData<*>>) -> Unit,
        val filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
    ) {
        companion object {
            fun of(
                menuButton: ITgMenuButton,
                filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
            ): Integration = Integration(listOf(menuButton), { _, _ -> }, filter)
            fun of(
                callbackName: String,
                menuDisplay: String,
                processor: suspend (TgCallbackQuery, SQLCallback<MenuCallbackData<*>>) -> Unit,
                filter: (Long, Long?) -> Boolean = { chatId, userId -> true },
            ): Integration = of(callbackName, menuDisplay, processor, MenuCallbackData.DummyAdditional::class.java, MenuCallbackData.DummyAdditional(), filter)
            fun <T> of(
                callbackName: String,
                menuDisplay: String,
                processor: suspend (TgCallbackQuery, SQLCallback<MenuCallbackData<*>>) -> Unit,
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
                            processor.invoke(cbq, sql)
                    },
                    filter = filter,
                )
            }
        }
    }

    suspend fun sendMenu(chatId: Long, userId: Long?, threadId: Int? = null) {
        val chat = SQLChat.get(chatId)
        val user = userId?.let { SQLEntity.get(it) }
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
        val entity = SQLEntity.get(cbq.from.id)?:return SUCCESS
        if (!entity.accountType.isPlayer) {
            ServerBot.bot.editMessageText(
                chatId = cbq.message.chat.id,
                messageId = cbq.message.messageId,
                text = ServerBot.config.integration.messageNotPlayer,
            )
            return DELETE_MARKUP
        }
        when (sql.data!!.operation /*cbq.data*/) {
            "back" -> sendMenu(cbq.message.chat.id, cbq.from.id, cbq.message.messageThreadId)
            "info" -> ServerBotLogic.sendInfoMessage(entity)
            else -> {
                integrations.map { it.menuCallbackProcessor }.forEach { it(cbq, sql) }
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