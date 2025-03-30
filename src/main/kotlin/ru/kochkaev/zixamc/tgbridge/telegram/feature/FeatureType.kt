package ru.kochkaev.zixamc.tgbridge.telegram.feature

import com.google.gson.annotations.JsonAdapter
import ru.kochkaev.zixamc.tgbridge.config.GsonManager
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.config.serialize.FeatureTypeAdapter
import ru.kochkaev.zixamc.tgbridge.telegram.model.*
import ru.kochkaev.zixamc.tgbridge.sql.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgMenu
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.FeatureData

@JsonAdapter(FeatureTypeAdapter::class)
open class FeatureType<R: FeatureData>(
    val model: Class<R>,
    val serializedName: String,
    val tgDisplayName: () -> String = { serializedName },
    val tgDescription: () -> String = { "" },
    val tgOnDone: suspend (SQLGroup) -> String = { "" },
    val checkAvailable: suspend (SQLGroup) -> Boolean = { true },
    val getDefault: (SQLGroup) -> R = { model.getDeclaredConstructor().newInstance(it) },
    val optionsResolver: (R) -> String = { "" }
) {
    open suspend fun setUp(cbq: TgCallbackQuery, group: SQLGroup): TgCBHandlerResult {
        bot.editMessageText(
            chatId = group.chatId,
            messageId = cbq.message.messageId,
            text = tgDescription()
        )
        bot.editMessageReplyMarkup(
            chatId = group.chatId,
            messageId = cbq.message.messageId,
            replyMarkup = TgMenu(listOf(
                listOf(SQLCallback.of(
                    display = config.integration.group.confirmSetUpFeature,
                    type = "group",
                    data = ServerBotGroup.GroupCallback(
                        operation = ServerBotGroup.Operations.CONFIRM_SETUP_FEATURE,
                        name = serializedName
                    ),
                    canExecute = ServerBotGroup.CAN_EXECUTE_ADMIN,
                )),
                listOf(CancelCallbackData(
                    asCallbackSend = CancelCallbackData.CallbackSend(
                        type = "group",
                        data = ServerBotGroup.GroupCallback(ServerBotGroup.Operations.SEND_FEATURES),
                        result = TgCBHandlerResult.DELETE_LINKED,
                    ),
                    canExecute = ServerBotGroup.CAN_EXECUTE_ADMIN,
                ).build())
            ))
        )
        return TgCBHandlerResult.DELETE_LINKED
    }
    open suspend fun finishSetUp(group: SQLGroup, replyTo: Int?): TgCBHandlerResult {
        if (!group.features.contains(this)) {
            group.features.set(this, getDefault(group))
            bot.sendMessage(
                chatId = group.chatId,
                text = tgOnDone(group),
                replyParameters = replyTo?.let { TgReplyParameters(it) }
            )
        }
        return TgCBHandlerResult.DELETE_MARKUP
    }
    open fun getResolvedOptions(data: FeatureData): String {
        return if (model.isInstance(data)) optionsResolver(model.cast(data)) else ""
    }
    open suspend fun sendEditor(cbq: TgCallbackQuery, group: SQLGroup): TgCBHandlerResult {
        getEditorMarkup(cbq, group).also {
            if (it.isEmpty()) return TgCBHandlerResult.SUCCESS
            try { bot.editMessageText(
                chatId = group.chatId,
                messageId = cbq.message.messageId,
                text = TextParser.formatLang(
                    text = config.integration.group.settings.featureDescription,
                    "feature" to tgDisplayName(),
                    "options" to getResolvedOptions(group.features.getCasted(this) as FeatureData)
                )
            ) } catch (_: Exception) {}
            try { bot.editMessageReplyMarkup(
                chatId = group.chatId,
                messageId = cbq.message.messageId,
                replyMarkup = TgMenu(it),
            ) } catch (_: Exception) {}
        }
        return TgCBHandlerResult.DELETE_LINKED
    }
    open fun getEditorMarkup(cbq: TgCallbackQuery, group: SQLGroup): ArrayList<List<SQLCallback.Companion.Builder<out CallbackData>>> = arrayListOf()
    open suspend fun processSetup(cbq: TgCallbackQuery, group: SQLGroup, cbd: ServerBotGroup.FeatureGroupCallback<R>): TgCBHandlerResult {
        return  TgCBHandlerResult.SUCCESS
    }
    @Suppress("UNCHECKED_CAST")
    suspend fun uncheckedProcessSetup(cbq: TgCallbackQuery, group: SQLGroup, cbd: Any) =
        processSetup(cbq, group, cbd as ServerBotGroup.FeatureGroupCallback<R>)

    fun with(data: R, mod: (R) -> R): R =
        GsonManager.gson.let { gson ->
            gson.fromJson(gson.toJson(data), model)
        } .let(mod)
}
