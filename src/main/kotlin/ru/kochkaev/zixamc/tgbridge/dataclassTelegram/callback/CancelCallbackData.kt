package ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import ru.kochkaev.zixamc.tgbridge.RequestsBot
import ru.kochkaev.zixamc.tgbridge.ServerBot
import ru.kochkaev.zixamc.tgbridge.TelegramBotZixa
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgCallbackQuery
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgChatMemberStatuses
import ru.kochkaev.zixamc.tgbridge.serverBot.group.ServerBotGroupUpdateManager
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLProcess
import java.lang.reflect.Type

data class CancelCallbackData(
    val cancelProcesses: List<ProcessType<*>> = listOf(),
    val asCallbackSend: CallbackSend<*>? = null,
    var canExecute: CallbackCanExecute = CallbackCanExecute(),
) : CallbackData {
    @JsonAdapter(CallbackSendAdapter::class)
    data class CallbackSend<T: CallbackData>(
        var type: String,
        var data: T,
        var result: TgCBHandlerResult = TgCBHandlerResult.DELETE_LINKED,
    )
    fun build() =
        SQLCallback.of(
            display = ServerBot.config.integration.group.backButton,
            type = "cancel",
            data = this,
        )
    companion object {
        val ON_SERVER_CALLBACK: suspend (TgCallbackQuery, SQLCallback<CancelCallbackData>) -> TgCBHandlerResult =
            { cbq, sql -> onCallback(cbq, sql, ServerBot.bot) }
        val ON_REQUESTS_CALLBACK: suspend (TgCallbackQuery, SQLCallback<CancelCallbackData>) -> TgCBHandlerResult =
            { cbq, sql -> onCallback(cbq, sql, RequestsBot.bot) }
        private suspend fun onCallback(cbq: TgCallbackQuery, sql: SQLCallback<CancelCallbackData>, bot: TelegramBotZixa): TgCBHandlerResult {
            if (sql.canExecute?.statuses?.contains(bot.getChatMember(cbq.message.chat.id, cbq.from.id).status) == true) {
                sql.data?.run {
                    this.cancelProcesses.forEach { SQLProcess.get(cbq.message.chat.id, it)?.drop() }
                    this.asCallbackSend?.also {
                        bot.typedCallbackQueryHandlers[it.type]?.invoke(cbq, SQLCallback.of("", it.type, it.data, this.canExecute).pull().let { id -> SQLCallback.get(id)!! })
                        return it.result
                    }
                }
            } else return ServerBotGroupUpdateManager.answerHaventRights(cbq.id, sql.canExecute?.display?:"")
            return TgCBHandlerResult.DELETE_MARKUP
        }
    }
    class CallbackSendAdapter : JsonDeserializer<CallbackSend<out CallbackData>>, JsonSerializer<CallbackSend<out CallbackData>> {

        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): CallbackSend<out CallbackData> {
            val jsonObject = json.asJsonObject
            val type = jsonObject.get("type").asString
//        val data = getDeserialized(context, jsonObject, typeMap[type])
            val data = context.deserialize<CallbackData>(
                jsonObject.get("data"),
                SQLCallback.registries[type],
            )
            val result = context.deserialize<TgCBHandlerResult>(
                jsonObject.get("result"),
                TgCBHandlerResult::class.java,
            )
            return CallbackSend(type, data, result)
        }
        override fun serialize(
            src: CallbackSend<out CallbackData>,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            val jsonObject = JsonObject()
            jsonObject.addProperty("type", src.type)
            jsonObject.add("data", context.serialize(src.data))
            jsonObject.add("result", context.serialize(src.result))
            return jsonObject
        }

    }
}
