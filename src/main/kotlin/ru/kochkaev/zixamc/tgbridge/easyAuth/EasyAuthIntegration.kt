package ru.kochkaev.zixamc.tgbridge.easyAuth

import com.google.gson.annotations.SerializedName
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.network.ServerPlayerEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.ServerBot
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgCallbackQuery
import ru.kochkaev.zixamc.tgbridge.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.CallbackData

object EasyAuthIntegration {

    private val onApproveHandlers = ArrayList<(SQLEntity, String)->Unit>()
    private val onDenyHandlers = ArrayList<(SQLEntity, String)->Unit>()

    val isEnabled: Boolean
        get() = FabricLoader.getInstance().isModLoaded("easyauth") && config.easyAuth.isEnabled

    fun registerOnTelegramApproveHandler(handler: (SQLEntity, String) -> Unit) {
        onApproveHandlers.add(handler)
    }
    fun registerOnTelegramDenyHandler(handler: (SQLEntity, String) -> Unit) {
        onDenyHandlers.add(handler)
    }

    fun isAuthenticated(nickname: String): Boolean =
        if (isEnabled && config.easyAuth.suppressMessagesWithoutAuth) AuthManager.isAuthenticated(nickname)
        else true
    fun isAuthenticated(player: ServerPlayerEntity): Boolean =
        if (isEnabled && config.easyAuth.suppressMessagesWithoutAuth) AuthManager.isAuthenticated(player)
        else true
    fun addToPrevious(player: ServerPlayerEntity?) {
        if (isEnabled) AuthManager.addToPrevious(player)
    }

    fun onJoin(player: ServerPlayerEntity) = ServerBot.withScopeAndLock {
        if (isEnabled) AuthManager.onJoin(player)
    }
    fun onLeave(player: ServerPlayerEntity) = ServerBot.withScopeAndLock {
        if (isEnabled) AuthManager.onLeave(player.nameForScoreboard)
    }

    fun registerEasyAuthHandlers() {
        EasyAuthCustomEvents.UPDATE_PLAYER_AUTHENTICATED_EVENT.register({ authenticated, player ->
            if (!authenticated) onJoin(player)
            else onLeave(player)
        })
    }

    suspend fun onTelegramCallbackQuery(cbq: TgCallbackQuery, /*data: TgCallback<EasyAuthCallbackData>*/) {
//        val args = cbq.data?.split(Regex("easyauth\$(.*?)/([a-zA-Z0-9_])"))
        val data = cbq.data?:return
        if (!cbq.data.startsWith("easyauth")) return
        val args = data.substring(data.indexOf('\$')+1, data.length)
        val nickname = args.substring(args.indexOf('/')+1, args.length)
        val operation = args.substring(0, args.indexOf('/'))
        if (!isEnabled) return
        val entity = SQLEntity.get(cbq.from.id)?:return
        when (/*data.data!!.operation*/ operation) {
            "approve" -> AuthManager.approve(entity, /*data.data.nickname*/nickname)
            "deny" -> AuthManager.deny(entity, /*data.data.nickname*/nickname)
        }
        entity.tempArray = entity.tempArray?.filter { it!=cbq.message.messageId.toString() }?.toTypedArray()
    }

    data class EasyAuthCallbackData(
        @SerializedName("n")
        val nickname: String,
        @SerializedName("o")
        val operation: String
    ) : CallbackData
}