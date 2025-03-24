package ru.kochkaev.zixamc.tgbridge.telegram.easyAuth

import net.minecraft.server.network.ServerPlayerEntity
import ru.kochkaev.zixamc.tgbridge.*
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.server
import xyz.nikitacartes.easyauth.EasyAuth.config as easyAuthConfig
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.telegram.BotLogic
import xyz.nikitacartes.easyauth.EasyAuth
import xyz.nikitacartes.easyauth.utils.PlayerAuth


object AuthManager {

    private val players: ArrayList<String> = arrayListOf()

    fun isAuthenticated(nickname: String) : Boolean =
        isAuthenticated(server.playerManager.getPlayer(nickname))
    fun isAuthenticated(player: ServerPlayerEntity?) : Boolean {
        val authenticated = (player as PlayerAuth?)?.`easyAuth$isAuthenticated`() ?: false
//        val canSkipAuth = (player as PlayerAuth?)?.`easyAuth$canSkipAuth`() ?: false
        val havePrevious = players.contains(player?.nameForScoreboard)
//        val isMojang = (player as PlayerAuth?)?.`easyAuth$isUsingMojangAccount`() ?: false
        return authenticated && havePrevious
    }

    fun addToPrevious(player: ServerPlayerEntity?) {
        if (player!=null && !players.contains(player.nameForScoreboard)) players.add(player.nameForScoreboard)
    }

    suspend fun approve(entity: SQLEntity, nickname: String) {
        val player = server.playerManager.getPlayer(nickname)
        val uuid = (player as PlayerAuth).`easyAuth$getFakeUuid`()
        (player as PlayerAuth).`easyAuth$setAuthenticated`(true)
        (player as PlayerAuth).`easyAuth$restoreLastLocation`()
        EasyAuth.playerCacheMap[uuid]?.loginTries?.set(0)
        player.sendMessage(config.easyAuth.langMinecraft.onApprove.getMinecraft())
        try {
            bot.sendMessage(
                chatId = entity.userId,
                text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.onApprove, nickname),
            )
        } catch (_: Exception) {}
    }
    suspend fun deny(entity: SQLEntity, nickname: String) {
        val player = server.playerManager.getPlayer(nickname)
        val uuid = (player as PlayerAuth).`easyAuth$getFakeUuid`()
        player.networkHandler.disconnect(config.easyAuth.langMinecraft.onDeny.getMinecraft())
        val cache = EasyAuth.playerCacheMap[uuid]
        cache?.lastKicked = System.currentTimeMillis()
        cache?.loginTries?.set(0)
        try {
            bot.sendMessage(
                chatId = entity.userId,
                text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.onDeny, nickname),
            )
        } catch (_: Exception) {}
    }
    suspend fun onJoin(player: ServerPlayerEntity) {
        val uuid = (player as PlayerAuth).`easyAuth$getFakeUuid`()
        val cache = EasyAuth.playerCacheMap[uuid]
        if (!easyAuthConfig.enableGlobalPassword && (cache == null || cache.password.isEmpty())) return
        val nickname = player.nameForScoreboard
        val entity = SQLEntity.get(nickname)?:return kickYouAreNotPlayer(player)
        if ((player as PlayerAuth).`easyAuth$canSkipAuth`() || (player as PlayerAuth).`easyAuth$isAuthenticated`()) return
        player.sendMessage(config.easyAuth.langMinecraft.onJoinTip.getMinecraft())
        try {
            val message = bot.sendMessage(
                chatId = entity.userId,
                text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.onJoinTip, nickname),
                replyMarkup = ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup(
                    listOf(
                        listOf(
                            ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                text = BotLogic.escapePlaceholders(
                                    config.easyAuth.langTelegram.buttonApprove,
                                    nickname
                                ),
//                            callback_data = TgCallback("easyauth", EasyAuthCallbackData(nickname, "approve")).serialize()
                                callback_data = "easyauth\$approve/$nickname"
                            ),
                            ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.buttonDeny, nickname),
//                            callback_data = TgCallback("easyauth", EasyAuthCallbackData(nickname, "deny")).serialize()
                                callback_data = "easyauth\$deny/$nickname"
                            ),
                        )
                    )
                )
            )
            entity.tempArray.add(message.messageId.toString())
        } catch (e: Exception) {
            val botUsername = config.easyAuth.langMinecraft.botUsername
            player.sendMessage(
                config.easyAuth.langMinecraft.noHaveChatWithBot.getMinecraft(
                    listOf("url" to "https://t.me/${botUsername.replace("@", "")}")
                )
            )
        }
    }
    suspend fun onLeave(nickname: String) {
        val entity = SQLEntity.get(nickname)?:return
        entity.tempArray.get()?.forEach {
            try {
                bot.editMessageReplyMarkup(
                    chatId = entity.userId,
                    messageId = Integer.parseInt(it),
                    replyMarkup = ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyMarkup(),
                )
            } catch (_: Exception) {}
        }
        entity.tempArray.set(listOf())
    }
    private fun kickYouAreNotPlayer(player: ServerPlayerEntity?) {
        player?.networkHandler?.disconnect(config.easyAuth.langMinecraft.youAreNotPlayer.getMinecraft())
        val uuid = (player as PlayerAuth).`easyAuth$getFakeUuid`()
        val cache = EasyAuth.playerCacheMap[uuid]
        cache?.loginTries?.set(0)
        cache?.lastKicked = System.currentTimeMillis()
    }
}