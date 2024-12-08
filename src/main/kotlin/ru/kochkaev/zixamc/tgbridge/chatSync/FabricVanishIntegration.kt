package ru.kochkaev.zixamc.tgbridge.chatSync

import me.drex.vanish.api.VanishAPI
import me.drex.vanish.api.VanishEvents
import net.kyori.adventure.text.Component
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity

object FabricVanishIntegration {

    fun registerOnJoinMessage(handler: (TBPlayerEventData) -> Unit) {
        VanishEvents.VANISH_EVENT.register { player, vanish ->
            if (!vanish) handler.invoke(TBPlayerEventData(
                player.displayName?.literalString?:player.name.string,
                Component.empty()
            ))
        }
    }
    fun registerOnLeaveMessage(handler: (TBPlayerEventData) -> Unit) {
        VanishEvents.VANISH_EVENT.register { player, vanish ->
            if (vanish) handler.invoke(TBPlayerEventData(
                player.displayName?.literalString?:player.name.string,
                Component.empty()
            ))
        }
    }
    fun isVanished(player: Entity): Boolean = VanishAPI.isVanished(player)

}