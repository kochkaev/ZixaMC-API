package ru.kochkaev.zixamc.tgbridge.chatSync

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import kotlinx.coroutines.*
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager
import java.time.Clock
import java.time.temporal.ChronoUnit
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore.lang
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore.config
import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.ServerBot.coroutineScope
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.Markdown2HTMLParser
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.*


object ChatSyncBotLogic {
    
    private val core = ChatSyncBotCore
    private var lastMessage: LastMessage? = null
    private val lastMessageLock = Mutex()

    suspend fun sendServerStartedMessage() {
        if (!ZixaMCTGBridge.tmp.isSilentRestart)
            core.sendMessage(lang.telegram.serverStarted)
        else {
            ZixaMCTGBridge.tmp.isSilentRestart = false
            ConfigManager.update()
            ConfigManager.load()
        }
    }
    suspend fun sendServerStoppedMessage() {
        if (!ZixaMCTGBridge.tmp.isSilentRestart)
            core.sendMessage(lang.telegram.serverStopped)
    }

    fun registerTelegramHandlers() {
        bot.registerCommandHandler("list", this::onTelegramListCommand)
        bot.registerMessageHandler(this::onTelegramMessage)
    }

    private suspend fun onTelegramListCommand(msg: TgMessage) {
        val onlinePlayerNames = core.getOnlinePlayerNames()
        if (onlinePlayerNames.isNotEmpty()) {
            bot.sendMessage(
                chatId = msg.chat.id,
                replyParameters = TgReplyParameters(msg.messageId),
                text = TextParser.formatLang(
                    lang.telegram.playerList,
                    "count" to onlinePlayerNames.size.toString(),
                    "usernames" to onlinePlayerNames.joinToString(),
                ),
            )
        } else {
            bot.sendMessage(
                chatId = msg.chat.id,
                replyParameters = TgReplyParameters(msg.messageId),
                text = lang.telegram.playerListZeroOnline,
            )
        }
    }

    private suspend fun onTelegramMessage(msg: TgMessage) {
        if (
            msg.chat.id != config.chatId
            || config.topicId != null && msg.messageThreadId != config.topicId
        ) {
            return
        }
        lastMessageLock.withLock {
            lastMessage = null
        }
        core.broadcastMessage(TextParser.toMinecraft(msg, bot.me.id))
    }

    fun registerMinecraftHandlers() {
//        core.registerCommand(arrayOf("tgbridge", "reload"), this::onReloadCommand)
        core.registerChatMessageListener(this::onChatMessage)
        core.registerSayMessageListener(this::onSayMessage)
        core.registerMeMessageListener(this::onMeMessage)
        core.registerPlayerDeathListener(this::onPlayerDeath)
        core.registerPlayerJoinListener(this::onPlayerJoin)
        core.registerPlayerLeaveListener(this::onPlayerLeave)
        core.registerPlayerAdvancementListener(this::onPlayerAdvancement)
    }

    private fun onReloadCommand(ctx: TBCommandContext): Boolean {
        try {
//            ConfigManager.reload()
        } catch (e: Exception) {
            ctx.reply("Error reloading config: " + (e.message ?: e.javaClass.name))
            return false
        }
        runBlocking {
            bot.recoverPolling(coroutineScope)
            if (lastMessageLock.isLocked) {
                lastMessageLock.unlock()
            }
        }
        ctx.reply("Config reloaded. Note that the bot token can't be changed without a restart")
        return true
    }

    private fun onChatMessage(e: TBPlayerEventData) {
        sendChatMessage(e, lang.telegram.chatMessage, true, true)
    }
    private fun onSayMessage(e: TBPlayerEventData) {
        sendChatMessage(e, lang.telegram.sayMessage, false, false)
    }
    private fun onMeMessage(e: TBPlayerEventData) {
        sendChatMessage(e, lang.telegram.meMessage, false, false)
    }
    suspend fun sendReply(text: String, username: String, chatId: Long, replyTo: Long?) =
        sendChatMessage(
            rawMinecraftText = text,
            username = username,
            base = lang.telegram.chatMessage,
            canMergeMessages = false,
            shouldKeepAsLast = false,
            chatId = chatId,
            replyTo = replyTo,
        )
    private fun sendChatMessage(
        e: TBPlayerEventData,
        base: String,
        canMergeMessages: Boolean = true,
        shouldKeepAsLast: Boolean = true,
        chatId: Long = config.chatId,
        replyTo: Long? = null
    ) = withScopeAndLock {
        sendChatMessage(
            rawMinecraftText = (e.text as TextComponent).content(),
            username = e.username,
            base = base,
            canMergeMessages = canMergeMessages,
            shouldKeepAsLast = shouldKeepAsLast,
            chatId = chatId,
            replyTo = replyTo,
        )
    }
    private suspend fun sendChatMessage(
        rawMinecraftText: String,
        username: String,
        base: String,
        canMergeMessages: Boolean = true,
        shouldKeepAsLast: Boolean = true,
        chatId: Long = config.chatId,
        replyTo: Long? = null
    ): TgMessage? {
        val bluemapLink = TextParser.asBluemapLinkOrNone(rawMinecraftText)
        val prefix = config.messages.requirePrefixInMinecraft ?: ""
        if (bluemapLink == null && !rawMinecraftText.startsWith(prefix)) {
            return null
        }

        val textWithoutPrefix = if (config.messages.keepPrefix) {
            rawMinecraftText
        } else {
            rawMinecraftText.removePrefix(prefix)
        }
        var escapedText = TextParser.escapeHTML(textWithoutPrefix)
        if (config.messages.parseMarkdownInMinecraftToTelegramMessages)
            escapedText = Markdown2HTMLParser.parse(escapedText)

        val currText = TextParser.formatLang(
            base,
            "username" to username,
            "text" to (bluemapLink ?: escapedText),
        )
//        val formattedComponent =
//            if (config.messages.parseMarkdownInMinecraftToTelegramMessages)
//                core.placeholderAPIInstance!!.parse(currText, core)
//            else Component.text(currText)

        val currDate = Clock.systemUTC().instant()

        val lm = lastMessage
//        if (config.messages.doNotSendDuplicatedMessages && formattedComponent == lm?.componentOfLastAppend)
//            return@withScopeAndLock
        if (
            canMergeMessages && replyTo == null
            && lm != null
            && lm.type == LastMessageType.TEXT
            && (lm.text + "\n" + currText).length <= 4000
            && currDate.minus((config.messages.mergeWindow ?: 0).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
//            val formatted: Pair<String, List<TgEntity>?> =
//                if (config.messages.parseMarkdownInMinecraftToTelegramMessages)
//                    FormattingParser.formatMinecraftComponent2TgEntity(formattedComponent, lm.text!!.length)
//                else Pair(currText, null)
//            if (formatted.second!=null)
//                lm.entities =
//                    if (lm.entities!=null)
//                        lm.entities!!.plus(formatted.second!!)
//                    else formatted.second
            lm.text += "\n" + /*formatted.first*/ currText
            lm.date = currDate
//            lm.componentOfLastAppend = formattedComponent
            return core.editMessageText(lm.id, lm.text!!, lm.entities)
        } else {
//            val formatted: Pair<String, List<TgEntity>?> =
//                if (config.messages.parseMarkdownInMinecraftToTelegramMessages)
//                    FormattingParser.formatMinecraftComponent2TgEntity(formattedComponent)
//                else Pair(currText, null)
//            val newMsg = core.sendMessage(/*formatted.first, formatted.second*/ currText)
            val newMsg = bot.sendMessage(
                chatId = chatId,
                text = currText,
                messageThreadId = if (replyTo==null) config.topicId else null,
                replyParameters = if (replyTo!=null) TgReplyParameters(replyTo.toInt()) else null,
            )
            lastMessage = if (shouldKeepAsLast) LastMessage(
                LastMessageType.TEXT,
                newMsg.messageId,
                currDate,
                text = /*formatted.first*/ currText,
//                entities = formatted.second,
//                componentOfLastAppend = formattedComponent
            ) else null
            return newMsg
        }
    }

    private fun onPlayerDeath(e: TBPlayerEventData) = withScopeAndLock {
        if (!config.events.enableDeathMessages) {
            return@withScopeAndLock
        }
        val component = e.text as TranslatableComponent
        /*sendMessageWithFormatting*/core.sendMessage(
            TextParser.formatLang(
                lang.telegram.playerDied,
                "deathMessage" to TextParser.translateComponent(component), "username" to e.username)
                .let {
                    if (config.messages.parseMarkdownInMinecraftToTelegramMessages) it
                    else TextParser.escapeHTML(it)
                }
        )
        lastMessage = null
    }

    private fun onPlayerJoin(e: TBPlayerEventData) = withScopeAndLock {
        if (!config.events.enableJoinMessages) {
            return@withScopeAndLock
        }
        val lm = lastMessage
        val currDate = Clock.systemUTC().instant()
        if (
            lm != null
            && lm.type == LastMessageType.LEAVE
            && lm.leftPlayer!! == e.username
            && currDate.minus((config.events.leaveJoinMergeWindow ?: 0).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            core.deleteMessage(lm.id)
        } else {
            /*sendMessageWithFormatting*/core.sendMessage(
                TextParser
                    .formatLang(lang.telegram.playerJoined, "username" to e.username)
                    .let {
                        if (config.messages.parseMarkdownInMinecraftToTelegramMessages) it
                        else TextParser.escapeHTML(it)
                    }
            )
        }
        lastMessage = null
    }

    private fun onPlayerLeave(e: TBPlayerEventData) = withScopeAndLock {
        if (!config.events.enableLeaveMessages) {
            return@withScopeAndLock
        }
        val message = TextParser.formatLang(lang.telegram.playerLeft, "username" to e.username)
//        val newMsg = sendMessageWithFormatting(message)
        val newMsg = core.sendMessage(message)
        lastMessage = LastMessage(
            LastMessageType.LEAVE,
            newMsg.messageId,
            Clock.systemUTC().instant(),
            leftPlayer = e.username
        )
    }

    private fun onPlayerAdvancement(e: TBPlayerEventData) = withScopeAndLock {
        val advancementsCfg = config.events.advancementMessages
        if (!advancementsCfg.enable) {
            return@withScopeAndLock
        }
        val component = e.text as TranslatableComponent
        val advancementTypeKey = component.key()
        val squareBracketsComponent = component.args()[1] as TranslatableComponent
        val advancementNameComponent = squareBracketsComponent.args()[0]
        val advancementName = TextParser.translateComponent(advancementNameComponent)
        val advancementDescription = if (advancementsCfg.showDescription) {
            advancementNameComponent.style().hoverEvent()?.let {
                val advancementTooltipComponent = it.value() as Component
                if (advancementTooltipComponent.children().size < 2) {
                    return@let null
                }
                TextParser.translateComponent(advancementTooltipComponent.children()[1])
            } ?: ""
        } else {
            ""
        }
        val langKey = when (advancementTypeKey) {
            "chat.type.advancement.task" -> {
                if (!advancementsCfg.enableTask) return@withScopeAndLock
                lang.telegram.advancements.regular
            }

            "chat.type.advancement.goal" -> {
                if (!advancementsCfg.enableGoal) return@withScopeAndLock
                lang.telegram.advancements.goal
            }

            "chat.type.advancement.challenge" -> {
                if (!advancementsCfg.enableChallenge) return@withScopeAndLock
                lang.telegram.advancements.challenge
            }

            else -> throw TBAssertionFailed("Unknown advancement type $advancementTypeKey.")
        }
        val message = TextParser.formatLang(
            langKey,
            "username" to e.username,
            "title" to
                    if (config.messages.parseMarkdownInMinecraftToTelegramMessages)
                        advancementName
                    else TextParser.escapeHTML(advancementName),
            "description" to
                    if (config.messages.parseMarkdownInMinecraftToTelegramMessages)
                        advancementDescription
                    else TextParser.escapeHTML(advancementDescription),
        )
//        sendMessageWithFormatting(message)
        core.sendMessage(message)
        lastMessage = null
    }

    private fun withScopeAndLock(fn: suspend () -> Unit) {
        coroutineScope.launch {
            lastMessageLock.withLock {
                fn()
            }
        }
    }
    private fun <T> withScopeAndLockReturnable(fn: suspend () -> T): T? {
        var out: T? = null
        coroutineScope.launch {
            lastMessageLock.withLock {
                out = fn()
            }
        }
        return out
    }

//    private suspend fun sendMessageWithFormatting(message: String) =
//        if (config.messages.parseMarkdownInMinecraftToTelegramMessages) {
////            val parsedComponent = (core.placeholderAPIInstance?.parse(message, core))?:Component.text(message)
////            val formatted = FormattingParser.formatMinecraftComponent2TgEntity(parsedComponent)
////            core.sendMessage(formatted.first, formatted.second)
//            core.sendMessage(Markdown2HTMLParser.parse(message))
//        } else core.sendMessage(message)
}