package ru.kochkaev.zixamc.tgbridge.telegram.feature

import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.api.telegram.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser
import ru.kochkaev.zixamc.api.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult.Companion.DELETE_LINKED
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.GroupCallback
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.Operations
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLProcess
import ru.kochkaev.zixamc.api.sql.callback.CallbackData
import ru.kochkaev.zixamc.api.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.sql.process.GroupSelectTopicProcessData
import ru.kochkaev.zixamc.api.sql.process.ProcessTypes
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.TopicFeatureData
import ru.kochkaev.zixamc.api.telegram.model.TgReplyParameters


open class TopicFeatureType<R: TopicFeatureData>(
    model: Class<R>,
    serializedName: String,
    tgDisplayName: () -> String = { serializedName },
    tgDescription: () -> String = { "" },
    tgOnDone: suspend (SQLGroup) -> String = { "" },
    checkAvailable: suspend (SQLGroup) -> Boolean = { true },
    getDefault: (SQLGroup) -> R = { model.getDeclaredConstructor().newInstance(null, it) },
    optionsResolver: (R) -> String = { "" }
): FeatureType<R>(model, serializedName, tgDisplayName, tgDescription, tgOnDone, checkAvailable, getDefault, optionsResolver) {
    override suspend fun setUp(cbq: TgCallbackQuery, group: SQLGroup): TgCBHandlerResult {
        bot.editMessageText(
            chatId = group.chatId,
            messageId = cbq.message.messageId,
            text = TextParser.formatLang(
                text = (tgDescription.invoke()) +
                        if (cbq.message.chat.isForum) "\n\n" + config.integration.group.selectTopicForFeature else "",
                "groupName" to group.name!!
            )
        )
        SQLProcess.get(group.chatId, ProcessTypes.GROUP_SELECT_TOPIC_FEATURE)?.run {
            this.data?.also { bot.deleteMessage(group.chatId, it.messageId) }
            this.drop()
        }
        if (cbq.message.chat.isForum)
            SQLProcess.of(ProcessTypes.GROUP_SELECT_TOPIC_FEATURE, GroupSelectTopicProcessData(cbq.message.messageId, serializedName)).pull(group.chatId)
        else {
            finishSetUp(group, cbq.message.messageId)
        }
        if (cbq.message.chat.isForum) bot.editMessageReplyMarkup(
            chatId = group.chatId,
            messageId = cbq.message.messageId,
            replyMarkup = TgMenu(listOf(
                listOf(
                    CancelCallbackData(
                    cancelProcesses = listOf(ProcessTypes.GROUP_SELECT_TOPIC_FEATURE),
                    asCallbackSend = CancelCallbackData.CallbackSend(
                        type = "group",
                        data = GroupCallback(
                            operation = Operations.SEND_FEATURES,
                            name = null,
                        ),
                        result = DELETE_LINKED,
                    ),
                    canExecute = ServerBotGroup.CAN_EXECUTE_ADMIN,
                ).build())
            ))
        )
        return DELETE_LINKED
    }

    open suspend fun finishSetUp(group: SQLGroup, replyTo: Int? = null, topicId: Int? = null) {
        if (!group.features.contains(this)) {
            group.features.set(this, getDefault(group).apply {
                this.topicId = topicId
            })
            bot.sendMessage(
                chatId = group.chatId,
                text = tgOnDone(group),
                replyParameters = replyTo?.let { TgReplyParameters(it) }
            )
        }
    }

    override fun getEditorMarkup(cbq: TgCallbackQuery, group: SQLGroup): ArrayList<List<SQLCallback.Companion.Builder<out CallbackData>>> {
        val origin = super.getEditorMarkup(cbq, group)
        if (cbq.message.chat.isForum) origin.add(listOf(SQLCallback.of(
            display = config.integration.group.settings.selectTopic,
            type = "group",
            data = GroupCallback(
                operation = Operations.TOPIC_RESELECT,
                name = this.serializedName,
            ),
            canExecute = ServerBotGroup.CAN_EXECUTE_ADMIN
        )))
        origin.add(listOf(
            CancelCallbackData(
            asCallbackSend = CancelCallbackData.CallbackSend(
                type = "group",
                data = GroupCallback(
                    operation = Operations.EDIT_FEATURE,
                    name = this.serializedName,
                ),
                result = DELETE_LINKED
            ),
            canExecute = ServerBotGroup.CAN_EXECUTE_ADMIN
        ).build()))
        return origin
    }

}