package ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import ru.kochkaev.zixamc.api.ZixaMC.Companion.server

object MinecraftAdventureConverter {
    private val registries: DynamicRegistryManager.Immutable
        get(){
            val server = server ?: return DynamicRegistryManager.of(Registries.REGISTRIES)
            return server.registryManager
        }
    fun adventureToMinecraft(adventure: Component): Text {
        val serializedTree = GsonComponentSerializer.gson().serializeToTree(adventure)
        return Text.Serialization.fromJsonTree(
            serializedTree,
            registries
        )!!
    }
    fun minecraftToAdventure(minecraft: Text): Component {
        val jsonString = Text.Serialization.toJsonString(
            minecraft,
            registries
        )
        return GsonComponentSerializer.gson().deserialize(jsonString)
    }
}