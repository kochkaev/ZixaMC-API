package ru.kochkaev.zixamc.tgbridge.telegram.serverBot.integration

import de.maxhenkel.audioplayer.AudioManager
import de.maxhenkel.audioplayer.AudioPlayer
import de.maxhenkel.audioplayer.FileNameManager
import kotlinx.coroutines.runBlocking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLProcess
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgMenu
import ru.kochkaev.zixamc.tgbridge.sql.process.ProcessData
import ru.kochkaev.zixamc.tgbridge.sql.process.ProcessTypes
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgMessage
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.integration.Menu.BACK_BUTTON
import java.nio.file.Path
import java.util.UUID

object AudioPlayerIntegration {

    val isModLoaded: Boolean
        get() = FabricLoader.getInstance().isModLoaded("audioplayer")

    val ruEnMap: Map<Char, String> = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e", 'ё' to "yo",
        'ж' to "zh", 'з' to "z", 'и' to "i", 'й' to "y", 'к' to "k", 'л' to "l", 'м' to "m",
        'н' to "n", 'о' to "o", 'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u",
        'ф' to "f", 'х' to "h", 'ц' to "ts", 'ч' to "ch", 'ш' to "sh", 'щ' to "shch",
        'ъ' to "", 'ы' to "y", 'ь' to "", 'э' to "e", 'ю' to "yu", 'я' to "ya"
    )

    fun resolveId(
        path: Path,
        server: MinecraftServer = FabricLoader.getInstance().gameInstance as MinecraftServer
    ): UUID {
        val uuid = UUID.randomUUID()
        AudioManager.saveSound(server, uuid, path)
        return uuid
    }

    fun resolveName(current: String): String {
        val dotIndex = current.lastIndexOf('.')
        val base = current.substring(0, dotIndex)
        val extension = current.substring(dotIndex).lowercase()
        val ruToEnBase = StringBuilder()
        base.toCharArray().forEach {
            if (ruEnMap.contains(it)) {
                val en = ruEnMap[it.lowercaseChar()] ?: ""
                ruToEnBase.append(if (it.isUpperCase()) en.replaceFirstChar { it1 -> it1.uppercaseChar() } else en)
            } else ruToEnBase.append(it)
        }
        val sanitizedBase = ruToEnBase.replace(Regex("[^a-z0-9_ \\-]", RegexOption.IGNORE_CASE), "")
        return sanitizedBase + extension
    }

    suspend fun callbackProcessor(cbq: TgCallbackQuery, sql: SQLCallback<Menu.MenuCallbackData<*>>) {
        val message = ServerBot.bot.sendMessage(
            chatId = cbq.message.chat.id,
            text = if (isModLoaded) ServerBot.config.integration.audioPlayer.messageUpload else ServerBot.config.integration.audioPlayer.modIsNodInstalled,
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
        if (isModLoaded) {
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

    suspend fun messageProcessor(msg: TgMessage, process: SQLProcess<*>, data: ProcessData) = runBlocking {
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
            resolveName(filename)
        val downloaded = ServerBot.bot.saveFile(fileId, "$path1/$resolvedFilename")
        val uuid = resolveId(
            path1.resolve(downloaded)
        )
        return if (downloaded.isNotEmpty()) uuid.toString() else ""
    }
}