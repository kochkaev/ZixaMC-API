package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.gson.annotations.JsonAdapter
import ru.kochkaev.zixamc.tgbridge.ServerBot
import ru.kochkaev.zixamc.tgbridge.config.serialize.FeatureTypeAdapter
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.*
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgMenu
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup

@JsonAdapter(FeatureTypeAdapter::class)
open class FeatureType<R: FeatureData>(
    val model: Class<R>,
    val serializedName: String,
    val tgDisplayName: () -> String = { serializedName },
    val tgDescription: () -> String = { "" },
    val checkAvailable: suspend (SQLGroup) -> Boolean = { true },
    val getDefault: () -> R = { model.getDeclaredConstructor().newInstance() },
    val optionsResolver: (R) -> String = { "" }
) {
    open suspend fun setUp(cbq: TgCallbackQuery, group: SQLGroup): TgCBHandlerResult {
        group.features.set(this, getDefault())
        return TgCBHandlerResult.DELETE_MARKUP
    }
    open fun getResolvedOptions(data: FeatureData): String {
        return if (model.isInstance(data)) optionsResolver(model.cast(data)) else ""
    }
    open suspend fun sendEditor(cbq: TgCallbackQuery, group: SQLGroup): TgCBHandlerResult {
        getEditorMarkup(cbq, group).also {
            if (it.isEmpty()) return TgCBHandlerResult.SUCCESS
            ServerBot.bot.editMessageReplyMarkup(
                chatId = group.chatId,
                messageId = cbq.message.messageId,
                replyMarkup = TgMenu(it).inline(),
            )
        }
        return TgCBHandlerResult.SUCCESS
    }
    open fun getEditorMarkup(cbq: TgCallbackQuery, group: SQLGroup): ArrayList<List<SQLCallback.Companion.Builder<out CallbackData>>> = arrayListOf()
}
