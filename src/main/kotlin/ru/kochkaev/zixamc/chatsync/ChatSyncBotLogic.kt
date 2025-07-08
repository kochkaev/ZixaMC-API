package ru.kochkaev.zixamc.chatsync

import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import kotlinx.coroutines.*
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import ru.kochkaev.zixamc.api.Initializer
import ru.kochkaev.zixamc.api.config.ConfigManager
import java.time.Clock
import java.time.temporal.ChronoUnit
import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.api.Initializer.coroutineScope
import ru.kochkaev.zixamc.api.ZixaMC
import ru.kochkaev.zixamc.api.escapeHTML
import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.chatsync.parser.Markdown2HTMLParser
import ru.kochkaev.zixamc.chatsync.parser.TextParser
import ru.kochkaev.zixamc.api.telegram.model.*
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.chatsync.settings.ChatSyncEditPrefixCallbackData
import ru.kochkaev.zixamc.chatsync.settings.ChatSyncFeatureType
import ru.kochkaev.zixamc.requests.ChatSyncSQLGroup
import kotlin.plus


@Suppress("CAST_NEVER_SUCCEEDS")
object ChatSyncBotLogic {

    val DEFAULT_GROUP = SQLGroup.get("main")!!

    suspend fun sendServerStartedMessage() {
        if (!ChatSync.isSilentRestart)
            bot.sendMessage(
                chatId = DEFAULT_GROUP.chatId,
                text = ChatSyncBotCore.lang.telegram.serverStarted,
                messageThreadId = DEFAULT_GROUP.features.getCasted(ChatSyncFeatureType)!!.topicId,
            )
        else {
            ChatSync.isSilentRestart = false
            Config.update()
            Config.load()
        }
    }
    suspend fun sendServerStoppedMessage() {
        if (!ChatSync.isSilentRestart)
            bot.sendMessage(
                chatId = DEFAULT_GROUP.chatId,
                text = ChatSyncBotCore.lang.telegram.serverStopped,
                messageThreadId = DEFAULT_GROUP.features.getCasted(ChatSyncFeatureType)!!.topicId,
            )
    }

    fun registerTelegramHandlers() {
        bot.registerCallbackQueryHandler($$"group$chatsync$editPrefix", ChatSyncEditPrefixCallbackData::class.java, ChatSyncEditPrefixCallbackData::onCallback)
        bot.registerCommandHandler("list", this::onTelegramListCommand)
        bot.registerMessageHandler(this::onTelegramMessage)
    }

    private suspend fun onTelegramListCommand(msg: TgMessage) {
        val onlinePlayerNames = ChatSyncBotCore.getOnlinePlayerNames()
        if (onlinePlayerNames.isNotEmpty()) {
            bot.sendMessage(
                chatId = msg.chat.id,
                replyParameters = TgReplyParameters(msg.messageId),
                text = ChatSyncBotCore.lang.telegram.playerList.formatLang(
                    "count" to onlinePlayerNames.size.toString(),
                    "usernames" to onlinePlayerNames.joinToString(),
                ),
            )
        } else {
            bot.sendMessage(
                chatId = msg.chat.id,
                replyParameters = TgReplyParameters(msg.messageId),
                text = ChatSyncBotCore.lang.telegram.playerListZeroOnline,
            )
        }
    }

    private suspend fun onTelegramMessage(msg: TgMessage) {
        val group = (SQLGroup.get(msg.chat.id) ?: return).also {
            if (it.features.getCasted(ChatSyncFeatureType)?.checkValidMsg(msg) != true) return
        }
        (group as ChatSyncSQLGroup).`chatsync$getLastMessageLock`().withLock {
            (group as ChatSyncSQLGroup).`chatsync$setLastMessage`(null)
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
            SQLGroup.groups.map { it as ChatSyncSQLGroup } .forEach {
                if (it.`chatsync$getLastMessageLock`().isLocked)
                    it.`chatsync$getLastMessageLock`().unlock()
            }
        }
        ctx.reply("Config reloaded. Note that the bot token can't be changed without a restart")
        return true
    }

    private fun onChatMessage(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) =
        if (!ChatSyncBotCore.config.addPrefixToChatMessages) {
            sendChatMessage(e, group, ChatSyncBotCore.lang.telegram.chatMessage, true, true)
            true
        }
        else {
            runBlocking {
                group.features.getCasted(ChatSyncFeatureType)?.broadcastMinecraft(e.username, (e.text as TextComponent).content())
            }
            false
        }
    private fun onSayMessage(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) {
        sendChatMessage(e, group, ChatSyncBotCore.lang.telegram.sayMessage, false, false)
    }
    private fun onMeMessage(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) {
        sendChatMessage(e, group, ChatSyncBotCore.lang.telegram.meMessage, false, false)
    }
    suspend fun sendReply(text: String, group: SQLGroup, username: String, replyTo: Int?) =
        sendChatMessage(
            rawMinecraftText = text,
            group = group,
            username = username,
            base = ChatSyncBotCore.lang.telegram.chatMessage,
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
    ) = withScopeAndLock(group) {
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
        val prefix = ChatSyncBotCore.config.messages.requirePrefixInMinecraft ?: ""
        if (bluemapLink == null && !rawMinecraftText.startsWith(prefix))
            return null

        val textWithoutPrefix = if (ChatSyncBotCore.config.messages.keepPrefix) {
            rawMinecraftText
        } else {
            rawMinecraftText.removePrefix(prefix)
        }
        var escapedText = textWithoutPrefix.escapeHTML()
        if (ChatSyncBotCore.config.messages.parseMarkdownInMinecraftToTelegramMessages)
            escapedText = Markdown2HTMLParser.parse(escapedText)

        val currText = base.formatLang(
            "username" to username,
            "text" to (bluemapLink ?: escapedText),
        )

        val currDate = Clock.systemUTC().instant()

        val lm = (group as ChatSyncSQLGroup).`chatsync$getLastMessage`()
        return if (
            canMergeMessages && replyTo == null
            && lm != null
            && lm.type == LastMessageType.TEXT
            && (lm.text + "\n" + currText).length <= 4000
            && currDate.minus((ChatSyncBotCore.config.messages.mergeWindow ?: 0).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            lm.text + "\n" + currText
            lm.date = currDate
            bot.editMessageText(
                chatId = group.chatId,
                messageId = lm.id,
                text = lm.text!!,
                entities = lm.entities
            )
        } else {
            val newMsg = bot.sendMessage(
                chatId = group.chatId,
                text = currText,
                messageThreadId = group.features.getCasted(ChatSyncFeatureType)!!.topicId,
                replyParameters = replyTo?.let { TgReplyParameters(it) }
            )
            (group as ChatSyncSQLGroup).`chatsync$setLastMessage`(if (shouldKeepAsLast) LastMessage(
                LastMessageType.TEXT,
                newMsg.messageId,
                currDate,
                text = currText,
            ) else null)
            newMsg
        }
    }

    private fun onPlayerDeath(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) = withScopeAndLock(group) {
        if (!ChatSyncBotCore.config.events.enableDeathMessages)
            return@withScopeAndLock
        val component = e.text as TranslatableComponent
        bot.sendMessage(
            chatId = group.chatId,
            text = ChatSyncBotCore.lang.telegram.playerDied.formatLang(
                "deathMessage" to TextParser.translateComponent(component), "username" to e.username)
                .let {
                    if (ChatSyncBotCore.config.messages.parseMarkdownInMinecraftToTelegramMessages) it
                    else it.escapeHTML()
                },
            messageThreadId = group.features.getCasted(ChatSyncFeatureType)!!.topicId
        )
        (group as ChatSyncSQLGroup).`chatsync$setLastMessage`(null)
    }

    private fun onPlayerJoin(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) = withScopeAndLock(group) {
        if (!ChatSyncBotCore.config.events.enableJoinMessages)
            return@withScopeAndLock
        val lm = (group as ChatSyncSQLGroup).`chatsync$getLastMessage`()
        val currDate = Clock.systemUTC().instant()
        if (
            lm != null
            && lm.type == LastMessageType.LEAVE
            && lm.leftPlayer!! == e.username
            && currDate.minus((ChatSyncBotCore.config.events.leaveJoinMergeWindow ?: 0).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            bot.deleteMessage(
                chatId = group.chatId,
                messageId = lm.id,
            )
        } else {
            bot.sendMessage(
                chatId = group.chatId,
                text = ChatSyncBotCore.lang.telegram.playerJoined
                    .formatLang("username" to e.username),
                messageThreadId = group.features.getCasted(ChatSyncFeatureType)!!.topicId
            )
        }
        (group as ChatSyncSQLGroup).`chatsync$setLastMessage`(null)
    }

    private fun onPlayerLeave(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) = withScopeAndLock(group) {
        if (!ChatSyncBotCore.config.events.enableLeaveMessages)
            return@withScopeAndLock
        val message = ChatSyncBotCore.lang.telegram.playerLeft.formatLang("username" to e.username)
        val newMsg = bot.sendMessage(
            chatId = group.chatId,
            text = message,
            messageThreadId = group.features.getCasted(ChatSyncFeatureType)!!.topicId
        )
        (group as ChatSyncSQLGroup).`chatsync$setLastMessage`(LastMessage(
            LastMessageType.LEAVE,
            newMsg.messageId,
            Clock.systemUTC().instant(),
            leftPlayer = e.username
        ))
    }

    private fun onPlayerAdvancement(e: TBPlayerEventData, group: SQLGroup = DEFAULT_GROUP) = withScopeAndLock(group) {
        val advancementsCfg = ChatSyncBotCore.config.events.advancementMessages
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
                ChatSyncBotCore.lang.telegram.advancements.regular
            }

            "chat.type.advancement.goal" -> {
                if (!advancementsCfg.enableGoal) return@withScopeAndLock
                ChatSyncBotCore.lang.telegram.advancements.goal
            }

            "chat.type.advancement.challenge" -> {
                if (!advancementsCfg.enableChallenge) return@withScopeAndLock
                ChatSyncBotCore.lang.telegram.advancements.challenge
            }

            else -> throw TBAssertionFailed("Unknown advancement type $advancementTypeKey.")
        }
        val message = langKey.formatLang(
            "username" to e.username,
            "title" to
                    if (ChatSyncBotCore.config.messages.parseMarkdownInMinecraftToTelegramMessages)
                        advancementName
                    else advancementName.escapeHTML(),
            "description" to
                    if (ChatSyncBotCore.config.messages.parseMarkdownInMinecraftToTelegramMessages)
                        advancementDescription
                    else advancementDescription.escapeHTML(),
        )
        bot.sendMessage(
            chatId = group.chatId,
            text = message,
            messageThreadId = group.features.getCasted(ChatSyncFeatureType)!!.topicId
        )
        (group as ChatSyncSQLGroup).`chatsync$setLastMessage`(null)
    }

    fun withScopeAndLock(group: SQLGroup, fn: suspend () -> Unit) {
        coroutineScope.launch {
            (group as ChatSyncSQLGroup).`chatsync$getLastMessageLock`().withLock { 
                fn()
            }
        }
    }
}