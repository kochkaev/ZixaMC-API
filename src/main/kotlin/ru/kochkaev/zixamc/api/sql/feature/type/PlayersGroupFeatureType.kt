package ru.kochkaev.zixamc.api.sql.feature.type

import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup.GroupCallback
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup.SetupFeatureCallback
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup.Operations
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.callback.CancelCallbackData
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.api.sql.callback.TgCBHandlerResult.Companion.DELETE_LINKED
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.api.telegram.ServerBot.config
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup.CAN_EXECUTE_ADMIN
import ru.kochkaev.zixamc.api.sql.feature.FeatureType
import ru.kochkaev.zixamc.api.sql.feature.data.PlayersGroupFeatureData
import ru.kochkaev.zixamc.api.telegram.model.*

object PlayersGroupFeatureType: FeatureType<PlayersGroupFeatureData>(
    model = PlayersGroupFeatureData::class.java,
    serializedName = "PLAYERS_GROUP",
    tgDisplayName = { config.group.features.playersGroup.display },
    tgDescription = { config.group.features.playersGroup.description },
    tgOnDone = { config.group.features.playersGroup.done },
    checkAvailable = { true },
    getDefault = { PlayersGroupFeatureData(group = it) },
    optionsResolver = {
        config.group.features.playersGroup.options.formatLang(
            "autoAccept" to config.group.settings.let { lang -> if (it.autoAccept) lang.truePlaceholder else lang.falsePlaceholder },
            "autoRemove" to config.group.settings.let { lang -> if (it.autoRemove) lang.truePlaceholder else lang.falsePlaceholder },
        )
    }
) {
    override fun getEditorMarkup(cbq: TgCallbackQuery, group: SQLGroup) = arrayListOf(
        listOf(
            SQLCallback.of(
            display = config.group.features.playersGroup.autoAccept,
            type = "group",
            data = GroupCallback.of(
                operation = Operations.SETUP_FEATURE,
                additionalType = SetupFeatureCallback::class.java,
                additional = SetupFeatureCallback(
                    feature = this,
                    temp = group.features.getCasted(this)?:getDefault(group),
                    field = "autoAccept",
                    arg = ""
                ),
            )
        )),
        listOf(
            SQLCallback.of(
                display = config.group.features.playersGroup.autoRemove,
                type = "group",
                data = GroupCallback.of(
                    operation = Operations.SETUP_FEATURE,
                    additionalType = SetupFeatureCallback::class.java,
                    additional = SetupFeatureCallback(
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
                data = if (!group.features.contains(this)) GroupCallback.of(Operations.SEND_FEATURES)
                    else GroupCallback.of(Operations.EDIT_FEATURE, serializedName),
                result = DELETE_LINKED
            ),
            canExecute = CAN_EXECUTE_ADMIN
        ).build())
    )

    override suspend fun processSetup(
        cbq: TgCallbackQuery,
        group: SQLGroup,
        cbd: GroupCallback<SetupFeatureCallback<PlayersGroupFeatureData>>
    ): TgCBHandlerResult {
        if (cbd.additional.arg.isEmpty()) when (cbd.additional.field) {
            "autoAccept" -> {
                bot.editMessageText(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    text = config.group.features.playersGroup.autoAccept
                )
                bot.editMessageReplyMarkup(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                SQLCallback.of(
                                    display = config.group.settings.turnOn,
                                    type = "group",
                                    data = GroupCallback.of(
                                        operation = Operations.SETUP_FEATURE,
                                        additionalType = SetupFeatureCallback::class.java,
                                        additional = SetupFeatureCallback(
                                            feature = this,
                                            temp = with(cbd.additional.temp) {
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
                                    display = config.group.settings.turnOff,
                                    type = "group",
                                    data = GroupCallback.of(
                                        operation = Operations.SETUP_FEATURE,
                                        additionalType = SetupFeatureCallback::class.java,
                                        additional = SetupFeatureCallback(
                                            feature = this,
                                            temp = with(cbd.additional.temp) {
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
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    text = config.group.features.playersGroup.autoRemove
                )
                bot.editMessageReplyMarkup(
                    chatId = group.id,
                    messageId = cbq.message.messageId,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                SQLCallback.of(
                                    display = config.group.settings.turnOn,
                                    type = "group",
                                    data = GroupCallback.of(
                                        operation = Operations.SETUP_FEATURE,
                                        additionalType = SetupFeatureCallback::class.java,
                                        additional = SetupFeatureCallback(
                                            feature = this,
                                            temp = with(cbd.additional.temp) {
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
                                    display = config.group.settings.turnOff,
                                    type = "group",
                                    data = GroupCallback.of(
                                        operation = Operations.SETUP_FEATURE,
                                        additionalType = SetupFeatureCallback::class.java,
                                        additional = SetupFeatureCallback(
                                            feature = this,
                                            temp = with(cbd.additional.temp) {
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
            group.features.set(this, cbd.additional.temp)
            sendEditor(cbq, group)
        }
        return DELETE_LINKED
    }
}