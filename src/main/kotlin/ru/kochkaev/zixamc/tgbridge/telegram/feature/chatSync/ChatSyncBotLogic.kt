package ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync

import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import kotlinx.coroutines.*
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager
import java.time.Clock
import java.time.temporal.ChronoUnit
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotCore.lang
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotCore.config
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.coroutineScope
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.Markdown2HTMLParser
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.telegram.model.*
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup


object ChatSyncBotLogic {

    val DEFAULT_GROUP = SQLGroup.get("main")!!

    suspend fun sendServerStartedMessage() {
        if (!ZixaMCTGBridge.tmp.isSilentRestart)
            DEFAULT_GROUP.sendMessage(lang.telegram.serverStarted)
        else {
            ZixaMCTGBridge.tmp.isSilentRestart = false
            ConfigManager.update()
            ConfigManager.load()
        }
    }
    suspend fun sendServerStoppedMessage() {
        if (!ZixaMCTGBridge.tmp.isSilentRestart)
            DEFAULT_GROUP.sendMessage(lang.telegram.serverStopped)
    }

    fun registerTelegramHandlers() {
        bot.registerCommandHandler("list", this::onTelegramListCommand)
        bot.registerMessageHandler(this::onTelegramMessage)
    }

    private suspend fun onTelegramListCommand(msg: TgMessage) {
        val onlinePlayerNames = ChatSyncBotCore.getOnlinePlayerNames()
        if (onlinePlayerNames.isNotEmpty()) {
            bot.sendMessage(
                chatId = msg.chat.id,
                replyParameters = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyParameters(msg.messageId),
                text = TextParser.formatLang(
                    lang.telegram.playerList,
                    "count" to onlinePlayerNames.size.toString(),
                    "usernames" to onlinePlayerNames.joinToString(),
                ),
            )
        } else {
            bot.sendMessage(
                chatId = msg.chat.id,
                replyParameters = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyParameters(msg.messageId),
                text = lang.telegram.playerListZeroOnline,
            )
        }
    }

    private suspend fun onTelegramMessage(msg: TgMessage) {
        val group = (SQLGroup.get(msg.chat.id) ?: return).also {
            if (!it.checkValidMsg(msg)) return
        }
        group.lastMessageLock.withLock {
            group.lastMessage = null
        }
        ChatSyncBotCore.broadcastMessage(TextParser.toMinecraft(msg, group, bot.me.id), group)
    }

    fun registerMinecraftHandlers() {
//        core.registerCommand(arrayOf("tgbridge", "reload"), this::onReloadCommand)
        ChatSyncBotCore.registerChatMessageListener(this::onChatMessage)
        ChatSyncBotCore.registerSayMessageListener(this::onSayMessage)
        ChatSyncBotCore.registerMeMessageListener(this::onMeMessage)
        ChatSyncBotCore.registerPlayerDeathListener(this::onPlayerDeath)
        ChatSyncBotCore.registerPlayerJoinListener(this::onPlayerJoin)
        ChatSyncBotCore.registerPlayerLeaveListener(this::onPlayerLeave)
        ChatSyncBotCore.registerPlayerAdvancementListener(this::onPlayerAdvancement)
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
            SQLGroup.groups.map { it.getSQLAssert() } .forEach {
                if (it.lastMessageLock.isLocked)
                    it.lastMessageLock.unlock()
            }
        }
        ctx.reply("Config reloaded. Note that the bot token can't be changed without a restart")
        return true
    }

    private fun onChatMessage(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) =
        if (!config.addPrefixToChatMessages) {
            sendChatMessage(e, group, lang.telegram.chatMessage, true, true)
            true
        }
        else {
            runBlocking {
                group.broadcastMinecraft(e.username, (e.text as TextComponent).content())
            }
            false
        }
    private fun onSayMessage(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) {
        sendChatMessage(e, group, lang.telegram.sayMessage, false, false)
    }
    private fun onMeMessage(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) {
        sendChatMessage(e, group, lang.telegram.meMessage, false, false)
    }
    suspend fun sendReply(text: String, group: SQLGroup, username: String, replyTo: Int?) =
        sendChatMessage(
            rawMinecraftText = text,
            group = group,
            username = username,
            base = lang.telegram.chatMessage,
            canMergeMessages = false,
            shouldKeepAsLast = false,
            replyTo = replyTo,
        )
    private fun sendChatMessage(
        e: TBPlayerEventData,
        group: SQLGroup,
        base: String,
        canMergeMessages: Boolean = true,
        shouldKeepAsLast: Boolean = true,
        replyTo: Int? = null
    ) = group.withScopeAndLock {
        sendChatMessage(
            rawMinecraftText = (e.text as TextComponent).content(),
            group = group,
            username = e.username,
            base = base,
            canMergeMessages = canMergeMessages,
            shouldKeepAsLast = shouldKeepAsLast,
            replyTo = replyTo,
        )
    }
    private suspend fun sendChatMessage(
        rawMinecraftText: String,
        group: SQLGroup,
        username: String,
        base: String,
        canMergeMessages: Boolean = true,
        shouldKeepAsLast: Boolean = true,
        replyTo: Int? = null
    ): TgMessage? {
        val bluemapLink = TextParser.asBluemapLinkOrNone(rawMinecraftText)
        val prefix = config.messages.requirePrefixInMinecraft ?: ""
        if (bluemapLink == null && !rawMinecraftText.startsWith(prefix))
            return null

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

        val currDate = Clock.systemUTC().instant()

        val lm = group.lastMessage
        return if (
            canMergeMessages && replyTo == null
            && lm != null
            && lm.type == LastMessageType.TEXT
            && (lm.text + "\n" + currText).length <= 4000
            && currDate.minus((config.messages.mergeWindow ?: 0).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            lm.text += "\n" + currText
            lm.date = currDate
            DEFAULT_GROUP.editMessageText(lm.id, lm.text!!, lm.entities)
        } else {
            val newMsg = group.sendMessage(
                text = currText,
                reply = replyTo?.toInt()
            )
            group.lastMessage = if (shouldKeepAsLast) LastMessage(
                LastMessageType.TEXT,
                newMsg.messageId,
                currDate,
                text = currText,
            ) else null
            newMsg
        }
    }

    private fun onPlayerDeath(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) = group.withScopeAndLock {
        if (!config.events.enableDeathMessages)
            return@withScopeAndLock
        val component = e.text as TranslatableComponent
        group.sendMessage(
            TextParser.formatLang(
                lang.telegram.playerDied,
                "deathMessage" to TextParser.translateComponent(component), "username" to e.username)
                .let {
                    if (config.messages.parseMarkdownInMinecraftToTelegramMessages) it
                    else TextParser.escapeHTML(it)
                }
        )
        group.lastMessage = null
    }

    private fun onPlayerJoin(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) = group.withScopeAndLock {
        if (!config.events.enableJoinMessages)
            return@withScopeAndLock
        val lm = group.lastMessage
        val currDate = Clock.systemUTC().instant()
        if (
            lm != null
            && lm.type == LastMessageType.LEAVE
            && lm.leftPlayer!! == e.username
            && currDate.minus((config.events.leaveJoinMergeWindow ?: 0).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            DEFAULT_GROUP.deleteMessage(lm.id)
        } else {
            group.sendMessage(
                TextParser
                    .formatLang(lang.telegram.playerJoined, "username" to e.username)
                    .let {
                        if (config.messages.parseMarkdownInMinecraftToTelegramMessages) it
                        else TextParser.escapeHTML(it)
                    }
            )
        }
        group.lastMessage = null
    }

    private fun onPlayerLeave(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) = group.withScopeAndLock {
        if (!config.events.enableLeaveMessages)
            return@withScopeAndLock
        val message = TextParser.formatLang(lang.telegram.playerLeft, "username" to e.username)
        val newMsg = group.sendMessage(message)
        group.lastMessage = LastMessage(
            LastMessageType.LEAVE,
            newMsg.messageId,
            Clock.systemUTC().instant(),
            leftPlayer = e.username
        )
    }

    private fun onPlayerAdvancement(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) = group.withScopeAndLock {
        val advancementsCfg = config.events.advancementMessages
        if (!advancementsCfg.enable) {
            return@withScopeAndLock
        }
        val component = e.text as TranslatableComponent
        val advancementTypeKey = component.key()
        val squareBracketsComponent = component.arguments()[1].asComponent() as TranslatableComponent
        val advancementNameComponent = squareBracketsComponent.arguments()[0].asComponent()
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
        group.sendMessage(message)
        group.lastMessage = null
    }
}