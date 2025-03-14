package ru.kochkaev.zixamc.tgbridge.serverBot.integration

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.runBlocking
import net.fabricmc.loader.api.FabricLoader
import ru.kochkaev.zixamc.tgbridge.BotLogic
import ru.kochkaev.zixamc.tgbridge.MySQLIntegration
import ru.kochkaev.zixamc.tgbridge.SQLEntity
import ru.kochkaev.zixamc.tgbridge.ServerBot
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgCallbackQuery
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCallback
import ru.kochkaev.zixamc.tgbridge.serverBot.ServerBotLogic
import java.io.File

object Menu {

    private var process: HashMap<Long, Processes> = hashMapOf()
    private enum class Processes {
        AUDIO_PLAYER;
    }

    private val isAudioPlayerLoaded = FabricLoader.getInstance().isModLoaded("audioplayer")

    suspend fun sendMenu(chatId: Long) {
        val entity = if (chatId>0) MySQLIntegration.getLinkedEntity(chatId) else null
        if (entity != null && entity.accountType.isPlayer()) {
            ServerBot.bot.sendMessage(
                chatId = chatId,
                text = ServerBot.config.integration.messageMenu,
                replyMarkup = TgInlineKeyboardMarkup(
                    listOf(
                        listOf(
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                ServerBot.config.integration.infoButton,
//                                callback_data = TgCallback("menu", MenuCallbackData("info")).serialize()
                                callback_data = "menu\$info"
                            )
                        ),
                        listOf(
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                ServerBot.config.integration.audioPlayer.buttonMenu,
//                                callback_data = TgCallback("menu", MenuCallbackData("audioPlayer")).serialize()
                                callback_data = "menu\$audioPlayer"
                            )
                        ),
                    )
                ),
            )
            process.remove(entity.userId)
        }
        else
            ServerBot.bot.sendMessage(
                chatId = chatId,
                text = ServerBot.config.integration.messageNotPlayer,
            )
    }
    suspend fun onCallback(cbq: TgCallbackQuery, /*data: TgCallback<MenuCallbackData>*/) {
        if (cbq.data == null || !cbq.data.startsWith("menu")) return
        val entity = MySQLIntegration.getLinkedEntity(cbq.from.id)?:return
        if (!entity.accountType.isPlayer()) {
            ServerBot.bot.editMessageText(
                chatId = cbq.message.chat.id,
                messageId = cbq.message.messageId,
                text = ServerBot.config.integration.messageNotPlayer,
            )
            return
        }
        when (/*data.data!!.operation*/ cbq.data) {
            "menu\$back" -> sendMenu(cbq.message.chat.id)
            "menu\$info" -> ServerBotLogic.sendInfoMessage(entity)
            "menu\$audioPlayer" -> {
                ServerBot.bot.sendMessage(
                    chatId = cbq.message.chat.id,
                    text = if (isAudioPlayerLoaded) ServerBot.config.integration.audioPlayer.messageUpload else ServerBot.config.integration.audioPlayer.modIsNodInstalled,
                    replyMarkup = TgInlineKeyboardMarkup(
                        listOf(
                            listOf(
                                TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                    ServerBot.config.integration.buttonBackToMenu,
//                                    callback_data = TgCallback("menu", MenuCallbackData("back")).serialize()
                                    callback_data = "menu\$back"
                                )
                            )
                        )
                    )
                )
                if (isAudioPlayerLoaded) process[cbq.message.chat.id] = Processes.AUDIO_PLAYER
            }
        }
    }
    suspend fun onMessage(msg: TgMessage) {
        runBlocking {
            val entity = if (msg.chat.id > 0) MySQLIntegration.getLinkedEntity(msg.chat.id) else null
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
                            replyMarkup = TgInlineKeyboardMarkup(
                                listOf(
                                    listOf(
                                        TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                            ServerBot.config.integration.buttonBackToMenu,
//                                            callback_data = TgCallback("menu", MenuCallbackData("back")).serialize()
                                            callback_data = "menu\$back"
                                        )
                                    )
                                )
                            )
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
        @SerializedName("o")
        val operation: String
    ) : CallbackData
}