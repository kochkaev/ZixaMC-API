package ru.kochkaev.zixamc.tgbridge.telegram.feature.type

import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.GroupCallback
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.FeatureGroupCallback
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.SetupFeatureCallback
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.Operations
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult.Companion.DELETE_LINKED
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCBHandlerResult.Companion.DELETE_MARKUP
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgMenu
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.CAN_EXECUTE_ADMIN
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureType
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.PlayersGroupFeatureData
import ru.kochkaev.zixamc.tgbridge.telegram.model.*

object PlayersGroupFeatureType: FeatureType<PlayersGroupFeatureData>(
    model = PlayersGroupFeatureData::class.java,
    serializedName = "PLAYERS_GROUP",
    tgDisplayName = { config.integration.group.features.playersGroup.display },
    tgDescription = { config.integration.group.features.playersGroup.description },
    tgOnDone = { config.integration.group.features.playersGroup.done },
    checkAvailable = { true },
    getDefault = { PlayersGroupFeatureData(group = it) },
    optionsResolver = {
        TextParser.formatLang(
            text = config.integration.group.features.playersGroup.options,
            "autoAccept" to config.integration.group.settings.let { lang -> if (it.autoAccept) lang.truePlaceholder else lang.falsePlaceholder },
            "autoRemove" to config.integration.group.settings.let { lang -> if (it.autoRemove) lang.truePlaceholder else lang.falsePlaceholder },
        )
    }
) {
    override fun getEditorMarkup(cbq: TgCallbackQuery, group: SQLGroup) = arrayListOf(
        listOf(
            SQLCallback.of(
            display = config.integration.group.features.playersGroup.autoAccept,
            type = "group",
            data = FeatureGroupCallback(
                data = SetupFeatureCallback(
                    feature = this,
                    temp = group.features.getCasted(this)?:getDefault(group),
                    field = "autoAccept",
                    arg = ""
                ),
            )
        )),
        listOf(
            SQLCallback.of(
                display = config.integration.group.features.playersGroup.autoRemove,
                type = "group",
                data = FeatureGroupCallback(
                    data = SetupFeatureCallback(
                        feature = this,
                        temp = group.features.getCasted(this)?:getDefault(group),
                        field = "autoRemove",
                        arg = ""
                    ),
                )
            )),
        listOf(CancelCallbackData(
            asCallbackSend = CancelCallbackData.CallbackSend(
                type = "group",
                data = if (!group.features.contains(this)) GroupCallback(Operations.SEND_FEATURES)
                    else GroupCallback(Operations.EDIT_FEATURE, serializedName),
                result = DELETE_LINKED
            ),
            canExecute = CAN_EXECUTE_ADMIN
        ).build())
    )

    override suspend fun processSetup(
        cbq: TgCallbackQuery,
        group: SQLGroup,
        cbd: FeatureGroupCallback<PlayersGroupFeatureData>
    ): TgCBHandlerResult {
        if (cbd.data.arg.isEmpty()) when (cbd.data.field) {
            "autoAccept" -> {
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = config.integration.group.features.playersGroup.autoAccept
                )
                bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                SQLCallback.of(
                                    display = config.integration.group.settings.turnOn,
                                    type = "group",
                                    data = FeatureGroupCallback(
                                        data = SetupFeatureCallback(
                                            feature = this,
                                            temp = with(cbd.data.temp) {
                                                it.autoAccept = true
                                                it
                                            },
                                            field = "autoAccept",
                                            arg = "true"
                                        ),
                                    )
                                )
                            ),
                            listOf(
                                SQLCallback.of(
                                    display = config.integration.group.settings.turnOff,
                                    type = "group",
                                    data = FeatureGroupCallback(
                                        data = SetupFeatureCallback(
                                            feature = this,
                                            temp = with(cbd.data.temp) {
                                                it.autoAccept = false
                                                it
                                            },
                                            field = "autoAccept",
                                            arg = "false"
                                        ),
                                    )
                                )
                            ),
                        )
                    )
                )
            }
            "autoRemove" -> {
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = config.integration.group.features.playersGroup.autoRemove
                )
                bot.editMessageReplyMarkup(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                SQLCallback.of(
                                    display = config.integration.group.settings.turnOn,
                                    type = "group",
                                    data = FeatureGroupCallback(
                                        data = SetupFeatureCallback(
                                            feature = this,
                                            temp = with(cbd.data.temp) {
                                                it.autoRemove = true
                                                it
                                            },
                                            field = "autoRemove",
                                            arg = "true"
                                        ),
                                    )
                                )
                            ),
                            listOf(
                                SQLCallback.of(
                                    display = config.integration.group.settings.turnOff,
                                    type = "group",
                                    data = FeatureGroupCallback(
                                        data = SetupFeatureCallback(
                                            feature = this,
                                            temp = with(cbd.data.temp) {
                                                it.autoRemove = false
                                                it
                                            },
                                            field = "autoRemove",
                                            arg = "false"
                                        ),
                                    )
                                )
                            ),
                        )
                    )
                )
            }
        } else {
            group.features.set(this, cbd.data.temp)
            sendEditor(cbq, group)
        }
        return DELETE_LINKED
    }
}