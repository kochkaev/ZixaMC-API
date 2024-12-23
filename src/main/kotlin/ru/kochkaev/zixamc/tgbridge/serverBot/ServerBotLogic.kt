package ru.kochkaev.zixamc.tgbridge.serverBot

import ru.kochkaev.zixamc.tgbridge.BotLogic
import ru.kochkaev.zixamc.tgbridge.SQLEntity
import ru.kochkaev.zixamc.tgbridge.RequestsBot.config as configRequests
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.ProtectedMessageData
import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters

object ServerBotLogic {

    suspend fun deleteProtected(
        protected: List<ProtectedMessageData>,
        protectLevel: Int,
    ) = BotLogic.deleteProtected(
        bot = bot,
        protected = protected,
        protectLevel = protectLevel,
    )

    suspend fun sendOnJoinInfoMessage(
        entity: SQLEntity,
        replyToMessageID: Int? = null,
    ) : TgMessage? = BotLogic.sendInfoMessage(
            bot = bot,
            chatId = entity.userId,
            replyParameters = if (replyToMessageID!=null) TgReplyParameters(replyToMessageID) else null,
            replyMarkup = TgInlineKeyboardMarkup(listOf(
                listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = configRequests.text.buttons.textButtonCopyServerIP,
                    copy_text = TgInlineKeyboardMarkup.TgInlineKeyboardButton.TgCopyTextButton(configRequests.serverIP),
                )),
            )),
            entity = entity,
        )

}