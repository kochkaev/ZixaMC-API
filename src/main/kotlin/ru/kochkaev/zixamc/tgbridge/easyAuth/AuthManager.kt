package ru.kochkaev.zixamc.tgbridge.easyAuth

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import ru.kochkaev.zixamc.tgbridge.*
import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.ServerBot.server
import xyz.nikitacartes.easyauth.EasyAuth.config as easyAuthConfig
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyMarkup
import xyz.nikitacartes.easyauth.EasyAuth
import xyz.nikitacartes.easyauth.utils.PlayerAuth


object AuthManager {

    fun isAuthenticated(nickname: String) : Boolean =
        isAuthenticated(server.playerManager.getPlayer(nickname))
    fun isAuthenticated(player: ServerPlayerEntity?) : Boolean =
        (player as PlayerAuth?)?.`easyAuth$isAuthenticated`()?:false

    suspend fun approve(entity: SQLEntity, nickname: String) {
        val player = server.playerManager.getPlayer(nickname)
        val uuid = (player as PlayerAuth).`easyAuth$getFakeUuid`()
        (player as PlayerAuth).`easyAuth$setAuthenticated`(true)
        (player as PlayerAuth).`easyAuth$restoreLastLocation`()
        EasyAuth.playerCacheMap[uuid]?.loginTries?.set(0)
        player.sendMessage(Text.of(BotLogic.escapePlaceholders(config.easyAuth.langMinecraft.onApprove, nickname)))
        bot.sendMessage(
            chatId = entity.userId,
            text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.onApprove, nickname),
        )
    }
    suspend fun deny(entity: SQLEntity, nickname: String) {
        val player = server.playerManager.getPlayer(nickname)
        val uuid = (player as PlayerAuth).`easyAuth$getFakeUuid`()
        player.networkHandler.disconnect(Text.of(BotLogic.escapePlaceholders(config.easyAuth.langMinecraft.onDeny, nickname)))
        val cache = EasyAuth.playerCacheMap[uuid]
        cache?.lastKicked = System.currentTimeMillis()
        cache?.loginTries?.set(0)
        bot.sendMessage(
            chatId = entity.userId,
            text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.onDeny, nickname),
        )
    }
    suspend fun onJoin(player: ServerPlayerEntity) {
        val uuid = (player as PlayerAuth).`easyAuth$getFakeUuid`()
        val cache = EasyAuth.playerCacheMap[uuid]
        if (!easyAuthConfig.enableGlobalPassword && (cache == null || cache.password.isEmpty())) return
        val nickname = player.nameForScoreboard
        val entity = MySQLIntegration.getLinkedEntityByNickname(nickname)?:return kickYouAreNotPlayer(player)
        if ((player as PlayerAuth).`easyAuth$canSkipAuth`() || (player as PlayerAuth).`easyAuth$isAuthenticated`()) return
        player.sendMessage(Text.of(BotLogic.escapePlaceholders(config.easyAuth.langMinecraft.onJoinTip, nickname)))
        val message = bot.sendMessage(
            chatId = entity.userId,
            text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.onJoinTip, nickname),
            replyMarkup = TgInlineKeyboardMarkup(
                listOf(listOf(
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                        text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.buttonApprove, nickname),
                        callback_data = "easyauth\$approve/$nickname"
                    ),
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                        text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.buttonDeny, nickname),
                        callback_data = "easyauth\$deny/$nickname"
                    ),
                ))
            )
        )
        entity.addToTempArray(message.messageId.toString())
    }
    suspend fun onLeave(nickname: String) {
        val entity = MySQLIntegration.getLinkedEntityByNickname(nickname)?:return
        entity.tempArray?.forEach {
            try {
                bot.editMessageReplyMarkup(
                    chatId = entity.userId,
                    messageId = Integer.parseInt(it),
                    replyMarkup = TgReplyMarkup(),
                )
            } catch (_: Exception) {}
        }
        entity.tempArray = arrayOf()
    }
    private fun kickYouAreNotPlayer(player: ServerPlayerEntity?) {
        player?.networkHandler?.disconnect(Text.of(BotLogic.escapePlaceholders(config.easyAuth.langMinecraft.youAreNotPlayer, player.nameForScoreboard)))
        val uuid = (player as PlayerAuth).`easyAuth$getFakeUuid`()
        val cache = EasyAuth.playerCacheMap[uuid]
        cache?.loginTries?.set(0)
        cache?.lastKicked = System.currentTimeMillis()
    }
}