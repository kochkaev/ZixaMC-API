package ru.kochkaev.zixamc.chatsync

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.network.ServerPlayerEntity
import ru.kochkaev.zixamc.easyauthintegration.AuthManager
import ru.kochkaev.zixamc.easyauthintegration.EasyAuthCustomEvents

object EasyAuthModuleIntegration {
    val enabled
        get() = FabricLoader.getInstance().isModLoaded("zixamc-easyauthintegration")

    fun registerUpdatePlayerAuthenticatedListener(handler: (Boolean, ServerPlayerEntity) -> Unit) {
        if (enabled)
            EasyAuthCustomEvents.UPDATE_PLAYER_AUTHENTICATED_EVENT.register { authenticated, player -> handler(authenticated, player) }
    }
    fun isAuthenticated(player: ServerPlayerEntity): Boolean {
        return if (enabled) AuthManager.isAuthenticated(player) else true
    }
    fun isAuthenticated(nickname: String): Boolean {
        return if (enabled) AuthManager.isAuthenticated(nickname) else true
    }
    fun addToPrevious(player: ServerPlayerEntity?) {
        if (enabled) AuthManager.addToPrevious(player)
    }
}