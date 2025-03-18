package ru.kochkaev.zixamc.tgbridge.chatSync.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.minecraft.util.Language
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessageMedia
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore.lang
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore.config
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup

object TextParser {

    fun escapeHTML(text: String): String = text
        .replace("&", "&amp;")
        .replace(">", "&gt;")
        .replace("<", "&lt;")

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

    fun formatLang(text: String, vararg args: Pair<String, String>): String {
        var res = text
        args.forEach {
            res = res.replace("{${it.first}}", it.second)
        }
        return res
    }

    fun trimReplyMessageText(text: String): String {
        val lines = text.split("\n", limit = 2)
        return if (lines.size > 1 || lines[0].length > 50) {
            lines[0].take(50) + "..."
        } else {
            lines[0]
        }
    }

    fun toMinecraft(message: TgMessage, group: SQLGroup, botId: Long): Component {
        val messages = mutableListOf<Component>()
        val components = mutableListOf<Component>()

        message.pinnedMessage?.also { pinnedMsg ->
            val pinnedMessageText = mutableListOf<Component>()
            forwardFromToText(pinnedMsg)?.also { pinnedMessageText.add(it) }
            mediaToText(pinnedMsg)?.also { pinnedMessageText.add(it) }
            message.effectiveText?.also { pinnedMessageText.add(TgEntities2TextComponentParser.parse(message, it, message.entities)) }
            components.add(
                lang.minecraft.pin.get(
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
        replyToText(message, group.topicId, resolveMessageLink(message), botId)?.also {
            if (!config.messages.replyInDifferentLine) components.add(it)
            else messages.add(it).also { messages.add(Component.text("\n")) }
        }
        mediaToText(message, resolveMessageLink(message))?.also { components.add(it) }
        message.effectiveText?.also {
            components.add(
                if (config.messages.styledTelegramMessagesInMinecraft)
                    TgEntities2TextComponentParser.parse(
                        message = message,
                        text = it,
                        entities = message.entities
                    )
                else Component.text(it)
            )
        }

        val senderNickname = SQLEntity.get(message.from?.id?:0)?.nickname
        messages.add(lang.minecraft.messageTGFormat.get(
            plainPlaceholders = listOf(
                "sender" to (senderNickname?:message.senderName),
            ),
            componentPlaceholders = listOf(
                "prefix" to group.getResolvedPrefix(message.messageId),
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
            media.animation to lang.minecraft.gif,
            media.document to lang.minecraft.document,
            media.photo to lang.minecraft.photo,
            media.audio to lang.minecraft.audio,
            media.sticker to lang.minecraft.sticker,
            media.video to lang.minecraft.video,
            media.videoNote to lang.minecraft.videoMessage,
            media.voice to lang.minecraft.voiceMessage,
        ).firstOrNull { it.first != null } ?.also {
            return it.second.get(listOf("url" to url))
        }
        media.poll?.also {
            return lang.minecraft.poll.get(listOf("title" to it.question, "url" to url))
        }

        return null
    }
    private data class ReplyInfo(
        var isReplyToMinecraft: Boolean,
        var senderName: String,
        var media: Component?,
        var text: String?,
    )
    fun replyToText(message: TgMessage, topicId: Int?, messageURL: String, botId: Long): Component? {
        var info: ReplyInfo? = null
        message.replyToMessage?.also { reply ->
            if (message.pinnedMessage != null || reply.messageId == topicId)
                return@also
            info = ReplyInfo(
                isReplyToMinecraft = reply.from?.id == botId,
                senderName = SQLEntity.get(reply.from?.id?:0)?.nickname ?: reply.senderName,
                media = mediaToText(reply, resolveMessageLink(message)),
                text = reply.effectiveText
            )
        }
        message.externalReply?.also { reply ->
            info = ReplyInfo(
                isReplyToMinecraft = false,
                senderName = reply.senderName,
                media = mediaToText(reply, resolveMessageLink(message)),
                text = null,
            )
        }
        message.quote?.also {
            info?.text = it.text
        }
        return info?.let {
            val fullText = "${it.media ?: ""} ${trimReplyMessageText(it.text ?: "")}".trim()
            if (it.isReplyToMinecraft) {
                lang.minecraft.replyToMinecraft.get(listOf("text" to fullText))
            } else {
                lang.minecraft.reply.get(
                    listOf(
                        "url" to messageURL,
                        "sender" to it.senderName,
                        "text" to fullText
                    )
                )
            }
        }
    }
    private fun forwardFromToText(message: TgMessage) =
        if (message.forwardFrom!=null) (
            message.forwardFrom.let {
                SQLEntity.get(it.id)?.nickname ?: message.senderUserName
            }
        ).let {
            lang.minecraft.forward.get(listOf("from" to it, "url" to resolveMessageLink(message)))
        } else null

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

                return """<a href="${config.messages.bluemapUrl}#$worldName:$x:$y:$z:50:0:0:0:0:perspective">$waypointName</a>"""
            } catch (_: NumberFormatException) {
            }
        }
        return null
    }
}