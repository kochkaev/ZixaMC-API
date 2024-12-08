package ru.kochkaev.zixamc.tgbridge.chatSync.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.text.Text

object MinecraftAdventureConverter {
    fun adventureToMinecraft(adventure: Component): Text {
        val serializedTree = GsonComponentSerializer.gson().serializeToTree(adventure)
        return Text.Serialization.fromJsonTree(
            serializedTree,
            DynamicRegistryManager.of(Registries.REGISTRIES))!!
    }

    fun minecraftToAdventure(minecraft: Text): Component {
        val jsonString = Text.Serialization.toJsonString(
            minecraft,
            DynamicRegistryManager.of(Registries.REGISTRIES)
        )
        return GsonComponentSerializer.gson().deserialize(jsonString)
    }
}