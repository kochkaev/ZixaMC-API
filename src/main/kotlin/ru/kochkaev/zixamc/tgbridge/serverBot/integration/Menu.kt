package ru.kochkaev.zixamc.tgbridge.serverBot.integration

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.runBlocking
import net.fabricmc.loader.api.FabricLoader
import ru.kochkaev.zixamc.tgbridge.ServerBot
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgCallbackQuery
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCBHandlerResult
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCBHandlerResult.Companion.DELETE_CALLBACK
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCBHandlerResult.Companion.DELETE_MARKUP
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCBHandlerResult.Companion.SUCCESS
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCallback
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgMenu
import ru.kochkaev.zixamc.tgbridge.serverBot.ServerBotLogic
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity

object Menu {

    private var process: HashMap<Long, Processes> = hashMapOf()
    private enum class Processes {
        AUDIO_PLAYER;
    }
    val BACK_BUTTON = SQLCallback.of(
        display = ServerBot.config.integration.buttonBackToMenu,
        type = "menu",
        data = MenuCallbackData("back")
    )

    private val isAudioPlayerLoaded = FabricLoader.getInstance().isModLoaded("audioplayer")

    suspend fun sendMenu(chatId: Long) {
        val entity = if (chatId>0) SQLEntity.get(chatId) else null
        if (entity != null && entity.accountType.isPlayer()) {
            ServerBot.bot.sendMessage(
                chatId = chatId,
                text = ServerBot.config.integration.messageMenu,
                replyMarkup = TgMenu(listOf(
                    listOf(SQLCallback.of(
                        display = ServerBot.config.integration.infoButton,
                        type = "menu",
                        data = MenuCallbackData("info")
                    )),
                    listOf(SQLCallback.of(
                        display = ServerBot.config.integration.audioPlayer.buttonMenu,
                        type = "menu",
                        data = MenuCallbackData("audioPlayer")
                    )),
                )).inline()
            )
            process.remove(entity.userId)
        }
        else
            ServerBot.bot.sendMessage(
                chatId = chatId,
                text = ServerBot.config.integration.messageNotPlayer,
            )
    }
    suspend fun onCallback(cbq: TgCallbackQuery, data: TgCallback<MenuCallbackData>): TgCBHandlerResult {
//        if (cbq.data == null || !cbq.data.startsWith("menu")) return
        val entity = SQLEntity.get(cbq.from.id)?:return SUCCESS
        if (!entity.accountType.isPlayer()) {
            ServerBot.bot.editMessageText(
                chatId = cbq.message.chat.id,
                messageId = cbq.message.messageId,
                text = ServerBot.config.integration.messageNotPlayer,
            )
            return DELETE_MARKUP
        }
        when (data.data!!.operation /*cbq.data*/) {
            "back" -> sendMenu(cbq.message.chat.id)
            "info" -> ServerBotLogic.sendInfoMessage(entity)
            "audioPlayer" -> {
                ServerBot.bot.sendMessage(
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
                    ))).inline()
                )
                if (isAudioPlayerLoaded) process[cbq.message.chat.id] = Processes.AUDIO_PLAYER
            }
        }
        return DELETE_MARKUP
    }
    suspend fun onMessage(msg: TgMessage) {
        runBlocking {
            val entity = if (msg.chat.id > 0) SQLEntity.get(msg.chat.id) else null
            if (entity != null && entity.accountType.isPlayer())
                when (process[entity.userId]) {
                    Processes.AUDIO_PLAYER -> {
                        var done = false
                        val message = ServerBot.bot.sendMessage(
                            chatId = msg.chat.id,
                            text = ServerBot.config.integration.audioPlayer.messagePreparing,
                        )
                        var filename: String? = null
                        if (msg.audio != null || msg.document != null) {
                            val tgFilename =
                                if (msg.audio != null)
                                    msg.audio.file_name ?: "${msg.audio.performer}_-_${msg.audio.title}.mp3"
                                else msg.document!!.file_name?:""
                            val extension = tgFilename.substring(tgFilename.lastIndexOf('.')+1).lowercase()
                            if (extension == "mp3" || extension == "wav")
                                filename = saveAudioPlayerFile(msg.audio?.file_id?:msg.document!!.file_id, tgFilename)
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
                            process.remove(entity.userId)
                        }
                        ServerBot.bot.editMessageReplyMarkup(
                            chatId = message.chat.id,
                            messageId = message.messageId,
                            replyMarkup = TgMenu(listOf(listOf(
                                BACK_BUTTON
                            ))).inline()
                        )
                    }

                    null -> {}
                }
        }
    }

    private suspend fun saveAudioPlayerFile(fileId: String, filename: String): String {
        val path = FabricLoader.getInstance().gameDir.toAbsolutePath()
        val path1 = path.resolve("$path/audioplayer_uploads/")
        path1.toFile().mkdirs()
        val resolvedFilename = AudioPlayerIntegration.resolveName(filename)
        val downloaded = ServerBot.bot.saveFile(fileId, "$path1/$resolvedFilename")
        val uuid = AudioPlayerIntegration.resolveId(path1.resolve(downloaded))
        return if (downloaded.isNotEmpty()) uuid.toString() else ""
    }

    data class MenuCallbackData(
        val operation: String
    ) : CallbackData
}