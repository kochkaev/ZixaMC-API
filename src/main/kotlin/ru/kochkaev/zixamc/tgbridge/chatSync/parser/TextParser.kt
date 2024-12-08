package ru.kochkaev.zixamc.tgbridge.chatSync.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.ClickEvent
import net.minecraft.util.Language
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessageMedia
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore.lang
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore.config
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage

object TextParser {

    private val core = ChatSyncBotCore

    fun escapeHTML(text: String): String = text
        .replace("&", "&amp;")
        .replace(">", "&gt;")
        .replace("<", "&lt;")

    private val fallbackMinecraftLangGetter = {
        key: String -> with(Language.getInstance()) { if (hasTranslation(key)) get(key) else null }
    }
//    private var minecraftLang: Map<String, String>? = null
    private val hardcodedDefaultMinecraftLang = mapOf(
        "gui.xaero-deathpoint-old" to "Old Death",
        "gui.xaero-deathpoint" to "Death",
    )

    fun getMinecraftLangKey(key: String): String? {
        return /*minecraftLang?.get(key)
            ?:*/ fallbackMinecraftLangGetter(key)
            ?: hardcodedDefaultMinecraftLang[key]
    }

    fun translateComponent(component: Component): String {
        return when (component) {
            is TranslatableComponent -> {
                var res = getMinecraftLangKey(component.key()) ?: component.key()
                // We're using older versions of kyori on some platforms, so using deprecated args() is ok
                component.args().forEachIndexed { i, x ->
                    val child = translateComponent(x)
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

    private fun mediaToText(media: TgMessageMedia): String? {
        listOf(
            media.animation to lang.minecraft.messageMeta.gif,
            media.document to lang.minecraft.messageMeta.document,
            media.photo to lang.minecraft.messageMeta.photo,
            media.audio to lang.minecraft.messageMeta.audio,
            media.sticker to lang.minecraft.messageMeta.sticker,
            media.video to lang.minecraft.messageMeta.video,
            media.videoNote to lang.minecraft.messageMeta.videoMessage,
            media.voice to lang.minecraft.messageMeta.voiceMessage,
        ).forEach {
            if (it.first != null) {
                return it.second
            }
        }

        media.poll?.let {
            return formatLang(lang.minecraft.messageMeta.poll, "title" to it.question)
        }

        return null
    }

    private data class ReplyInfo(
        var isReplyToMinecraft: Boolean,
        var senderName: String,
        var media: String?,
        var text: String?,
    )

    private fun replyToText(message: TgMessage, botId: Long): String? {
        var info: ReplyInfo? = null
        message.replyToMessage?.let { reply ->
            if (
            // Telegram sends reply message when message is pinned
                message.pinnedMessage != null
                // All messages to a topic are sent as replies to a service message
                || reply.messageId == config.topicId
            ) {
                return@let
            }
            info = ReplyInfo(
                isReplyToMinecraft = reply.from?.id == botId,
                senderName = reply.senderName,
                media = mediaToText(reply),
                text = reply.effectiveText
            )
        }
        message.externalReply?.let { reply ->
            info = ReplyInfo(
                isReplyToMinecraft = false,
                senderName = reply.senderName,
                media = mediaToText(reply),
                text = null,
            )
        }
        message.quote?.let {
            info?.text = it.text
        }

        return info?.let {
            val fullText = "${it.media ?: ""} ${trimReplyMessageText(it.text ?: "")}".trim()
            if (it.isReplyToMinecraft) {
                formatLang(lang.minecraft.messageMeta.replyToMinecraft, "text" to fullText)
            } else {
                formatLang(
                    lang.minecraft.messageMeta.reply,
                    "sender" to it.senderName,
                    "text" to fullText,
                )
            }
        }
    }

    private fun forwardFromToText(message: TgMessage): String? {
        val forwardFromName = message.forwardFrom?.let { _ ->
            (message.forwardFrom.firstName + " " + (message.forwardFrom.lastName ?: "")).trim()
        } ?: message.forwardFromChat?.let {
            message.forwardFromChat.title
        }
        return forwardFromName?.let {
            formatLang(lang.minecraft.messageMeta.forward, "from" to it)
        }
    }

    fun toMinecraft(message: TgMessage, botId: Long): Component {
        val components = mutableListOf<Component>()

//    components.add(Component.text("<${this.senderName}>", NamedTextColor.AQUA))

        message.pinnedMessage?.let { pinnedMsg ->
            val pinnedMessageText = mutableListOf<String>()
            forwardFromToText(pinnedMsg)?.let { pinnedMessageText.add(it) }
            mediaToText(pinnedMsg)?.let { pinnedMessageText.add(it) }
//        pinnedMsg.effectiveText?.let { pinnedMessageText.add(it) }
            message.effectiveText?.let { pinnedMessageText.add(it) }
            components.add(
                StyleManager.applyStyle(
                    component = Component.text(lang.minecraft.messageMeta.pin + " " + pinnedMessageText.joinToString(" ")),
                    color = lang.minecraft.messageFormatting.mediaColor,
                    decorations = lang.minecraft.messageFormatting.mediaFormatting,
                    hover = Component.text(lang.minecraft.messageMeta.hoverOpenInTelegram),
                    clickEvent = ClickEvent.openUrl(resolveMessageLink(message))
                )
            )
        }

        forwardFromToText(message)?.let { components.add(
            StyleManager.applyStyle(
                component = Component.text(it),
                color = lang.minecraft.messageFormatting.forwardColor,
                decorations = lang.minecraft.messageFormatting.forwardFormatting,
                hover = Component.text(lang.minecraft.messageMeta.hoverOpenInTelegram),
                clickEvent = ClickEvent.openUrl(resolveMessageLink(message))
            )
        ) }
        replyToText(message, botId)?.let {
            val replyText = StyleManager.applyStyle(
                component = Component.text(it),
                color = lang.minecraft.messageFormatting.replyColor,
                decorations = lang.minecraft.messageFormatting.replyFormatting,
                hover = Component.text(lang.minecraft.messageMeta.hoverOpenInTelegram),
                clickEvent = ClickEvent.openUrl(resolveMessageLink(message))
            )
            if (!config.messages.replyInDifferentLine) components.add(replyText)
            else ChatSyncBotCore.broadcastMessage(replyText)
        }
        mediaToText(message)?.let { components.add(
            StyleManager.applyStyle(
                component = Component.text(it),
                color = lang.minecraft.messageFormatting.mediaColor,
                decorations = lang.minecraft.messageFormatting.mediaFormatting,
                hover = Component.text(lang.minecraft.messageMeta.hoverOpenInTelegram),
                clickEvent = ClickEvent.openUrl(resolveMessageLink(message))
            )
        ) }
        message.effectiveText?.let { components.add((if (config.messages.styledTelegramMessagesInMinecraft) TgEntities2TextComponentParser.parse(
            message,
            it,
            message.entities
        ) else Component.text(it))) }

        return Component.text(lang.minecraft.messageMeta.messageFormat)
            .replaceText {it.matchLiteral("{sender}")
                .replacement(
                    Component.text(message.senderName)
                        .clickEvent(ClickEvent.suggestCommand("@${message.senderUserName}"))
                        .hoverEvent(Component.text(lang.minecraft.messageMeta.hoverTagToReply).asHoverEvent())
                )}
            .replaceText { it.matchLiteral("{text}").replacement(components
                .flatMap { component -> listOf(component, Component.text(" ")) }
                .fold(Component.text()) { acc, component -> acc.append(component) }
                .build()) }
    }

    fun resolveMessageLink(message: TgMessage): String = "https://t.me/c/${-message.chat.id-1000000000000}/" + (if (message.messageThreadId!=null) "${message.messageThreadId}/" else "") + "${message.messageId}"

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