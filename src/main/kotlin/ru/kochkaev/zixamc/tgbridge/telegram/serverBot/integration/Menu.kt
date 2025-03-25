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
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup

object Menu {

    val BACK_BUTTON = SQLCallback.of(
        display = ServerBot.config.integration.buttonBackToMenu,
        type = "menu",
        data = MenuCallbackData("back")
    )

    private val isAudioPlayerLoaded = FabricLoader.getInstance().isModLoaded("audioplayer")

    suspend fun sendMenu(chatId: Long, userId: Long?) {
        val chat = SQLChat.get(chatId)
        val user = userId?.let { SQLEntity.get(it) }
        if (user != null && chat != null && user.hasProtectedLevel(AccountType.PLAYER)) {
            ServerBot.bot.sendMessage(
                chatId = chatId,
                text = ServerBot.config.integration.messageMenu,
                replyMarkup = TgMenu(listOf(
                    if (chatId == userId) listOf(SQLCallback.of(
                        display = ServerBot.config.integration.infoButton,
                        type = "menu",
                        data = MenuCallbackData("info")
                    )) else listOf(),
                    if (chatId == userId) listOf(TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                        text = ServerBot.config.integration.addToGroupButton,
                        url = "https://t.me/${ServerBot.bot.me.username}?startgroup"
//                        switch_inline_query = ""
                    )) else listOf(),
                    listOf(SQLCallback.of(
                        display = ServerBot.config.integration.audioPlayer.buttonMenu,
                        type = "menu",
                        data = MenuCallbackData("audioPlayer")
                    )),
                ))
            )
//            process.remove(chat.getChatId())
        }
        else
            ServerBot.bot.sendMessage(
                chatId = chatId,
                text = ServerBot.config.integration.messageNotPlayer,
            )
    }
    suspend fun onCallback(cbq: TgCallbackQuery, sql: SQLCallback<MenuCallbackData>): TgCBHandlerResult {
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
            "back" -> sendMenu(cbq.message.chat.id, cbq.from.id)
            "info" -> ServerBotLogic.sendInfoMessage(entity)
            "audioPlayer" -> {
                val message = ServerBot.bot.sendMessage(
                    chatId = cbq.message.chat.id,
                    text = if (isAudioPlayerLoaded) ServerBot.config.integration.audioPlayer.messageUpload else ServerBot.config.integration.audioPlayer.modIsNodInstalled,
//                    replyMarkup = TgInlineKeyboardMarkup(
//                        listOf(
//                            listOf(
//                                TgInlineKeyboardMarkup.TgInlineKeyboardButton(
//                                    ServerBot.config.integration.buttonBackToMenu,
////                                    callback_data = TgCallback("menu", MenuCallbackData("back")).serialize()
//                                    callback_data = "menu\$back"
//                                )
//                            )
//                        )
//                    )
                    replyMarkup = TgMenu(listOf(listOf(
                        BACK_BUTTON
                    )))
                )
                if (isAudioPlayerLoaded) {
                    SQLProcess.get(cbq.message.chat.id, ProcessTypes.MENU_AUDIO_PLAYER_UPLOAD)?.also {
                        it.data?.run {
                            try { ServerBot.bot.editMessageReplyMarkup(
                                chatId = cbq.message.chat.id,
                                messageId = this.messageId,
                                replyMarkup = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyMarkup()
                            ) } catch (_: Exception) {}
                            SQLCallback.dropAll(cbq.message.chat.id, this.messageId)
                        }
                    } ?.drop()
                    SQLProcess.of(
                        type = ProcessTypes.MENU_AUDIO_PLAYER_UPLOAD,
                        data = ProcessData(message.messageId)
                    ).pull(cbq.message.chat.id)
                }
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
    suspend fun audioPlayerProcessor(msg: TgMessage, process: SQLProcess<*>, data: ProcessData) = runBlocking {
        if (msg.replyToMessage==null || msg.replyToMessage.messageId != data.messageId) return@runBlocking
        var done = false
        val message = ServerBot.bot.sendMessage(
            chatId = msg.chat.id,
            text = ServerBot.config.integration.audioPlayer.messagePreparing,
            replyParameters = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyParameters(msg.messageId)
        )
        var filename: String? = null
        if (msg.audio != null || msg.document != null) {
            val tgFilename =
                if (msg.audio != null)
                    msg.audio.file_name ?: "${msg.audio.performer}_-_${msg.audio.title}.mp3"
                else msg.document!!.file_name?:""
            val extension = tgFilename.substring(tgFilename.lastIndexOf('.')+1).lowercase()
            if (extension == "mp3" || extension == "wav")
                filename =
                    saveAudioPlayerFile(
                        msg.audio?.file_id ?: msg.document!!.file_id, tgFilename
                    )
            else {
                ServerBot.bot.editMessageText(
                    chatId = message.chat.id,
                    messageId = message.messageId,
                    text = ServerBot.config.integration.audioPlayer.messageIncorrectExtension,
                )
                done = true
            }
        } else {
            ServerBot.bot.editMessageText(
                chatId = message.chat.id,
                messageId = message.messageId,
                text = ServerBot.config.integration.audioPlayer.messageIncorrectExtension,
            )
            done = true
        }
        if (filename.isNullOrEmpty() && !done) {
            ServerBot.bot.editMessageText(
                chatId = message.chat.id,
                messageId = message.messageId,
                text = ServerBot.config.integration.audioPlayer.messageErrorUpload,
            )
        } else if (!done) {
            ServerBot.bot.editMessageText(
                chatId = message.chat.id,
                messageId = message.messageId,
                text = ServerBot.config.integration.audioPlayer.messageDone.replace(
                    "{filename}",
                    filename!!
                ),
            )
            try {
                ServerBot.bot.editMessageReplyMarkup(
                    chatId = message.chat.id,
                    messageId = data.messageId,
                    replyMarkup = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyMarkup(),
                )
                SQLCallback.dropAll(message.chat.id, data.messageId)
            } catch (_: Exception) {}
            process.drop()
        }
        ServerBot.bot.editMessageReplyMarkup(
            chatId = message.chat.id,
            messageId = message.messageId,
            replyMarkup = TgMenu(listOf(listOf(
                BACK_BUTTON
            )))
        )
    }

    private suspend fun saveAudioPlayerFile(fileId: String, filename: String): String {
        val path = FabricLoader.getInstance().gameDir.toAbsolutePath()
        val path1 = path.resolve("$path/audioplayer_uploads/")
        path1.toFile().mkdirs()
        val resolvedFilename =
            AudioPlayerIntegration.resolveName(filename)
        val downloaded = ServerBot.bot.saveFile(fileId, "$path1/$resolvedFilename")
        val uuid = AudioPlayerIntegration.resolveId(
            path1.resolve(downloaded)
        )
        return if (downloaded.isNotEmpty()) uuid.toString() else ""
    }

    data class MenuCallbackData(
        val operation: String
    ) : CallbackData
}