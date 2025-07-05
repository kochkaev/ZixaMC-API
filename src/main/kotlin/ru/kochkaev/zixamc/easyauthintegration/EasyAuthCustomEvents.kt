package ru.kochkaev.zixamc.easyauthintegration

import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.server.network.ServerPlayerEntity

class EasyAuthCustomEvents {
    companion object {
        val UPDATE_PLAYER_AUTHENTICATED_EVENT = EventFactory.createArrayBacked(UpdatePlayerAuthenticated::class.java) { handlers ->
            UpdatePlayerAuthenticated { authenticated, player ->
                for (handler in handlers) {
                    handler.onUpdatePlayerAuthenticated(authenticated, player)
                }
            }
        }
    }

    fun interface UpdatePlayerAuthenticated {
        fun onUpdatePlayerAuthenticated(authenticated: Boolean, player: ServerPlayerEntity): Unit
    }
}