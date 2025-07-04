package ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotCore.lang
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser.resolveMessageLink
import ru.kochkaev.zixamc.api.telegram.model.TgEntity
import ru.kochkaev.zixamc.api.telegram.model.TgEntityType
import ru.kochkaev.zixamc.api.telegram.model.TgMessage

object TgEntities2TextComponentParser {

    fun parse(message: TgMessage, text: String, entities: List<TgEntity>?) =
        parse(resolveMessageLink(message), text, entities)
    fun parse(messageLink: String?, text: String, entities: List<TgEntity>?): TextComponent {
        if (entities == null) return Component.text(text)
        val components = mutableListOf<TextComponent>()
        val currentEntities = ArrayList<TgEntity>()
        val nextEntities = ArrayList<TgEntity>()
        var isLegacy = false
        var isSpoiler = false
//        var previousIsSpoiler = false
        var spoiler: SpoilerComponent? = null
        var tempText = ""
        entities.forEach { if (it.offset == 0) {
            currentEntities.add(it)
            nextEntities.remove(it)
        }}
        for (i in text.indices) {
            tempText += text[i]
            entities.forEach {
                if (it.offset!! + it.length!! == i+1) {
                    isLegacy = true
                    nextEntities.remove(it)
                }
                if (it.offset == i+1) {
                    isLegacy = true
                    nextEntities.add(it)
                }
            }
            if (isLegacy || i == text.length-1) {
                isLegacy = false
                var tempComponent  = Component.text(tempText).toBuilder()
                currentEntities.forEach {
                    when (it.type) {
                        TgEntityType.BOLD -> tempComponent.decoration(TextDecoration.BOLD, true)
                        TgEntityType.ITALIC -> tempComponent.decoration(TextDecoration.ITALIC, true)
                        TgEntityType.UNDERLINE -> tempComponent.decoration(TextDecoration.UNDERLINED, true)
                        TgEntityType.STRIKETHROUGH -> tempComponent.decoration(TextDecoration.STRIKETHROUGH, true)
                        TgEntityType.TEXT_LINK -> tempComponent = lang.minecraft.link.getTextComponent(
                            plainPlaceholders = listOf("url" to it.url!!),
                            componentPlaceholders = listOf("title" to tempComponent.build()),
                        ).toBuilder()
                        TgEntityType.URL -> tempComponent = lang.minecraft.link.getTextComponent(
                            plainPlaceholders = listOf("url" to tempText),
                            componentPlaceholders = listOf("title" to tempComponent.build()),
                        ).toBuilder()
                        TgEntityType.MENTION -> tempComponent = lang.minecraft.mention.getTextComponent(
                            plainPlaceholders = listOf("mention" to tempText),
                            componentPlaceholders = listOf("title" to tempComponent.build())
                        ).toBuilder()
                        TgEntityType.HASHTAG, TgEntityType.CASHTAG -> tempComponent = lang.minecraft.hashtag.getTextComponent(
                            plainPlaceholders = listOf("url" to (messageLink?:"")),
                            componentPlaceholders = listOf("title" to tempComponent.build())
                        ).toBuilder()
                        TgEntityType.SPOILER -> isSpoiler = true
                        TgEntityType.CODE, TgEntityType.PRE -> tempComponent = lang.minecraft.code.getTextComponent(
                            plainPlaceholders = listOf("text" to tempText),
                            componentPlaceholders = listOf("title" to tempComponent.build()),
                        ).toBuilder()
                        else -> {}
                    }
                }
                if (isSpoiler && spoiler!=null) spoiler.append(tempComponent.build(), tempText)
                else if (isSpoiler) spoiler = SpoilerComponent().apply { this.append(tempComponent.build(), tempText) }
                else if (spoiler==null) components.add(tempComponent.build())
                if (spoiler!=null && (!isSpoiler || i == text.length-1)) {
                    components.add(spoiler.build())
                    spoiler = null
                }
//                if (isSpoiler) {
//                    if (previousIsSpoiler) {
//                        tempComponent = FormattingManager.appendToSpoilerComponent(
//                            components.last(),
//                            tempComponent.build()
//                        ).toBuilder()
//                        components.removeLast()
//                    }
//                    else {
//                        tempComponent = FormattingManager.getAsSpoilerComponent(tempComponent.build()).toBuilder()
//                        previousIsSpoiler = true
//                    }
//                    components.add(tempComponent.build())
//                }
//                else {
//                    if (previousIsSpoiler) previousIsSpoiler = false
//                    components.add(tempComponent.build())
//                }
                isSpoiler = false
                tempText = ""
                currentEntities.clear()
                currentEntities.addAll(nextEntities)
            }
        }
        return components.fold(Component.text()) { acc, component -> acc.append(component) } .build()
    }

    class SpoilerComponent {
        private val list = arrayListOf<TextComponent>()
        private val rawList = arrayListOf<String>()
        fun build() = lang.minecraft.spoiler.getTextComponent(
            plainPlaceholders = listOf("placeholder" to lang.minecraft.spoilerReplaceWithChar?.repeat(rawList.joinToString("").length).toString()),
            componentPlaceholders = listOf("text" to list.fold(Component.text()) { acc, it -> acc.append(it) } .build())
        )
        fun append(component: TextComponent, raw: String) {
            list.add(component)
            rawList.add(raw)
        }
    }
}