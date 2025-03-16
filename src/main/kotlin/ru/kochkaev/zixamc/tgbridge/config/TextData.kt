package ru.kochkaev.zixamc.tgbridge.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.MinecraftAdventureConverter

data class TextData (
    val raw: String
) {
    fun get(
        plainPlaceholders: List<Pair<String, String>> = emptyList(),
        componentPlaceholders: List<Pair<String, Component>> = emptyList(),
    ): Component {
        var res = raw
        plainPlaceholders.forEach {
            res = res.replace("{${it.first}}", mm.escapeTags(it.second))
        }
        return mm.deserialize(
            res,
            *plainPlaceholders.map { Placeholder.unparsed(it.first, it.second) }.toTypedArray(),
            *componentPlaceholders.map { Placeholder.component(it.first, it.second) }.toTypedArray()
        )
    }
    fun getTextComponent(
        plainPlaceholders: List<Pair<String, String>> = emptyList(),
        componentPlaceholders: List<Pair<String, Component>> = emptyList(),
    ) = get(plainPlaceholders, componentPlaceholders) as TextComponent
    fun getMinecraft(
        plainPlaceholders: List<Pair<String, String>> = emptyList(),
        componentPlaceholders: List<Pair<String, Component>> = emptyList(),
    ) = MinecraftAdventureConverter.adventureToMinecraft(get(plainPlaceholders, componentPlaceholders))

    companion object {
        private val mm = MiniMessage.miniMessage()
    }
}