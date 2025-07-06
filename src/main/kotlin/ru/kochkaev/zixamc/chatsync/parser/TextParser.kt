package ru.kochkaev.zixamc.chatsync.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorType
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.minecraft.util.Language
import ru.kochkaev.zixamc.api.telegram.model.TgMessageMedia
import ru.kochkaev.zixamc.api.telegram.model.TgMessage
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.telegram.model.TgEntity
import ru.kochkaev.zixamc.chatsync.ChatSyncBotCore
import ru.kochkaev.zixamc.chatsync.settings.ChatSyncFeatureType

object TextParser {

    private val fallbackMinecraftLangGetter = {
        key: String -> with(Language.getInstance()) { if (hasTranslation(key)) get(key) else null }
    }
    private val hardcodedDefaultMinecraftLang = mapOf(
        "gui.xaero-deathpoint-old" to "Old Death",
        "gui.xaero-deathpoint" to "Death",
    )

    fun getMinecraftLangKey(key: String): String? {
        return fallbackMinecraftLangGetter(key)
            ?: hardcodedDefaultMinecraftLang[key]
    }

    fun translateComponent(component: Component): String {
        return when (component) {
            is TranslatableComponent -> {
                var res = getMinecraftLangKey(component.key()) ?: component.key()
                component.arguments().forEachIndexed { i, x ->
                    val child = translateComponent(x.asComponent())
                    if (i == 0) {
                        res = res.replace("%s", child)
                    }
                    res = res.replace("%${i + 1}\$s", child)
                }
                res
            }

            is TextComponent -> {
                val children = component.children().joinToString("") {
                    translateComponent(it)
                }
                component.content() + children
            }

            else -> this.toString()
        }
    }

    fun trimReplyMessageText(text: String): String {
        val lines = text.split("\n", limit = 2)
        return if (lines.size > 1 || lines[0].length > 50) {
            lines[0].take(50) + "..."
        } else {
            lines[0]
        }
    }
    fun trimReplyMessageText(text: Component): Component {
        val iterator = text.iterable(ComponentIteratorType.BREADTH_FIRST)
        val folded = arrayListOf<Component>()
        var countKeys = 0
        iterator.forEach {
            val translated = translateComponent(it)
            val keys = translated.length
            if (countKeys == 0 || (countKeys + keys <=50 && folded.last() != it)) {
                countKeys += keys
                folded.add(it)
            }
        }
        if (countKeys>50) folded.add(Component.text("..."))
        return folded.fold(Component.text("")) { aac, it -> aac.append(it) }
    }

    fun toMinecraft(message: TgMessage, group: SQLGroup, botId: Long): Component {
        val messages = mutableListOf<Component>()
        val components = mutableListOf<Component>()

//        topicToText(message)?.also { components.add(it) }

        message.pinnedMessage?.also { pinnedMsg ->
            val pinnedMessageText = mutableListOf<Component>()
            forwardFromToText(pinnedMsg)?.also { pinnedMessageText.add(it) }
            mediaToText(pinnedMsg)?.also { pinnedMessageText.add(it) }
            message.effectiveText?.also { pinnedMessageText.add(
                TgEntities2TextComponentParser.parse(
                    message,
                    it,
                    message.entities
                )
            ) }
            components.add(
                ChatSyncBotCore.lang.minecraft.pin.get(
                    plainPlaceholders = listOf(
                        "url" to resolveMessageLink(message)
                    ),
                    componentPlaceholders = listOf(
                        "text" to pinnedMessageText.fold(Component.text()) { acc, it1 -> acc.append(it1) } .build()
                    )
                )
            )
        }

        forwardFromToText(message)?.also { components.add(it) }
        replyToText(message, group.features.getCasted(ChatSyncFeatureType)!!.topicId, resolveMessageLink(message), botId)?.also {
            if (!ChatSyncBotCore.config.messages.replyInDifferentLine) components.add(it)
            else messages.add(it).also { messages.add(Component.text("\n")) }
        }
        mediaToText(message, resolveMessageLink(message))?.also { components.add(it) }
        message.effectiveText?.also {
            components.add(
                if (ChatSyncBotCore.config.messages.styledTelegramMessagesInMinecraft)
                    TgEntities2TextComponentParser.parse(
                        message = message,
                        text = it,
                        entities = message.entities
                    )
                else Component.text(it)
            )
        }

        val senderNickname = SQLUser.get(message.from?.id?:0)?.nickname
        messages.add(
            ChatSyncBotCore.lang.minecraft.messageTGFormat.get(
            plainPlaceholders = listOf(
                "sender" to (senderNickname?:message.senderName),
            ),
            componentPlaceholders = listOf(
                "prefix" to (group.features.getCasted(ChatSyncFeatureType)?.getResolvedPrefix(message.messageId) ?: Component.text() as Component),
                "text" to components
                    .flatMap { component -> listOf(component, Component.text(" ")) }
                    .fold(Component.text()) { acc, component -> acc.append(component) }
                    .build()
            )
        ))
        return messages
            .fold(Component.text()) { acc, component -> acc.append(component) }
            .build()
    }
    private fun mediaToText(media: TgMessageMedia, url: String = "about:blank"): Component? {
        listOf(
            media.animation to ChatSyncBotCore.lang.minecraft.gif,
            media.document to ChatSyncBotCore.lang.minecraft.document,
            media.photo to ChatSyncBotCore.lang.minecraft.photo,
            media.audio to ChatSyncBotCore.lang.minecraft.audio,
            media.sticker to ChatSyncBotCore.lang.minecraft.sticker,
            media.video to ChatSyncBotCore.lang.minecraft.video,
            media.videoNote to ChatSyncBotCore.lang.minecraft.videoMessage,
            media.voice to ChatSyncBotCore.lang.minecraft.voiceMessage,
        ).firstOrNull { it.first != null } ?.also {
            return it.second.get(listOf("url" to url))
        }
        media.poll?.also {
            return ChatSyncBotCore.lang.minecraft.poll.get(listOf("title" to it.question, "url" to url))
        }

        return null
    }
    private data class ReplyInfo(
        var isReplyToMinecraft: Boolean,
        var senderName: String,
        var media: Component?,
        var text: String?,
        var entities: List<TgEntity>?,
        var messageLink: String?,
    )
    fun replyToText(message: TgMessage, topicId: Int?, messageURL: String, botId: Long): Component? {
        var info: ReplyInfo? = null
        message.replyToMessage?.also { reply ->
            if (message.pinnedMessage != null || reply.messageId == topicId || reply.forumTopicCreated != null)
                return@also
            info = ReplyInfo(
                isReplyToMinecraft = reply.from?.id == botId,
                senderName = SQLUser.get(reply.from?.id?:0)?.nickname ?: reply.senderName,
                media = mediaToText(reply, resolveMessageLink(message)),
                text = reply.effectiveText,
                entities = reply.entities,
                resolveMessageLink(reply)
            )
        }
        message.externalReply?.also { reply ->
            info = ReplyInfo(
                isReplyToMinecraft = false,
                senderName = reply.senderName,
                media = mediaToText(reply, resolveMessageLink(message)),
                text = null,
                entities = null,
                messageLink = resolveMessageLink(message),
            )
        }
        message.quote?.also {
            info?.text = it.text
            info?.entities = it.entities
        }
        return info?.let {
            val components = arrayListOf(
                it.media,
//                it.text?.let { txt -> trimReplyMessageText(
//                    if (config.messages.styledTelegramMessagesInMinecraft)
//                        TgEntities2TextComponentParser.parse(
//                            messageLink = it.messageLink,
//                            text = txt,
//                            entities = it.entities
//                        )
//                    else Component.text(txt)
//                ) },
                it.text?.let { txt ->
                    if (ChatSyncBotCore.config.messages.styledTelegramMessagesInMinecraft)
                        TgEntities2TextComponentParser.parse(
                            messageLink = it.messageLink,
                            text = txt,
                            entities = it.entities
                        )
                    else Component.text(txt)
                }
            )
            val fullText = components.filterNotNull()
                .flatMap { component -> listOf(component, Component.text(" ")) }
                .fold(Component.text("")) { aac, component ->
                    aac.append(component)
                }
            if (it.isReplyToMinecraft) {
                ChatSyncBotCore.lang.minecraft.replyToMinecraft.get(listOf(), listOf("text" to fullText))
            } else {
                ChatSyncBotCore.lang.minecraft.reply.get(
                    listOf(
                        "url" to messageURL,
                        "sender" to it.senderName,
                    ),
                    listOf(
                        "text" to fullText
                    )
                )
            }
        }
    }
    private fun forwardFromToText(message: TgMessage) =
        message.forwardFrom?.let {
            ChatSyncBotCore.lang.minecraft.forward.get(listOf("from" to (SQLUser.get(it.id)?.nickname ?: message.senderUserName), "url" to resolveMessageLink(message)))
        }
//    fun topicToText(message: TgMessage) =
//        SQLGroup.get(message.chat.id)?.features?.getCasted(FeatureTypes.CHAT_SYNC)?.let {
//            if (it.topicId != message.messageThreadId && message.replyToMessage?.forumTopicCreated != null)
//                config.lang.minecraft.topic.get(
//                    plainPlaceholders = listOf(
//                        "group" to it.group!!.name!!,
//                        "topicId" to message.messageThreadId.toString(),
//                        "topicName" to message.replyToMessage.forumTopicCreated.name
//                    )
//                )
//            else null
//        }

    fun resolveMessageLink(message: TgMessage): String =
        "https://t.me/c/${-message.chat.id-1000000000000}/" + (if (message.messageThreadId!=null) "${message.messageThreadId}/" else "") + "${message.messageId}"

    val XAERO_WAYPOINT_RGX =
        Regex("""xaero-waypoint:([^:]+):[^:]:([-\d]+):([-\d]+|~):([-\d]+):\d+:(?:false|true):\d+:Internal-(?:the-)?(overworld|nether|end)-waypoints""")

    fun asBluemapLinkOrNone(text: String): String? {
        XAERO_WAYPOINT_RGX.matchEntire(text)?.let {
            try {
                var waypointName = it.groupValues[1]
                if (waypointName == "gui.xaero-deathpoint-old" || waypointName == "gui.xaero-deathpoint") {
                    waypointName = translateComponent(Component.translatable(waypointName))
                }
                val x = Integer.parseInt(it.groupValues[2])
                val yRaw = it.groupValues[3]
                val y = Integer.parseInt(if (yRaw == "~") "100" else yRaw)
                val z = Integer.parseInt(it.groupValues[4])
                val worldName = it.groupValues[5]

                return """<a href="${ChatSyncBotCore.config.messages.bluemapUrl}#$worldName:$x:$y:$z:50:0:0:0:0:perspective">$waypointName [$x, $y, $z]</a>"""
            } catch (_: NumberFormatException) {
            }
        }
        return null
    }
}