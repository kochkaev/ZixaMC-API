package ru.kochkaev.zixamc.tgbridge.easyAuth

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import ru.kochkaev.zixamc.tgbridge.*
import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.ServerBot.server
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.MinecraftAdventureConverter
import xyz.nikitacartes.easyauth.EasyAuth.config as easyAuthConfig
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyMarkup
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCallback
import ru.kochkaev.zixamc.tgbridge.easyAuth.EasyAuthIntegration.EasyAuthCallbackData
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
        player.sendMessage(Text.of(BotLogic.escapePlaceholders(config.easyAuth.langMinecraft.onApprove, nickname)))
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
        player.networkHandler.disconnect(Text.of(BotLogic.escapePlaceholders(config.easyAuth.langMinecraft.onDeny, nickname)))
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
        val entity = MySQLIntegration.getLinkedEntityByNickname(nickname)?:return kickYouAreNotPlayer(player)
        if ((player as PlayerAuth).`easyAuth$canSkipAuth`() || (player as PlayerAuth).`easyAuth$isAuthenticated`()) return
        player.sendMessage(Text.of(BotLogic.escapePlaceholders(config.easyAuth.langMinecraft.onJoinTip, nickname)))
        try {
            val message = bot.sendMessage(
                chatId = entity.userId,
                text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.onJoinTip, nickname),
                replyMarkup = TgInlineKeyboardMarkup(
                    listOf(listOf(
                        TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.buttonApprove, nickname),
//                            callback_data = TgCallback("easyauth", EasyAuthCallbackData(nickname, "approve")).serialize()
                            callback_data = "easyauth\$approve/$nickname"
                        ),
                        TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                            text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.buttonDeny, nickname),
//                            callback_data = TgCallback("easyauth", EasyAuthCallbackData(nickname, "deny")).serialize()
                            callback_data = "easyauth\$deny/$nickname"
                        ),
                    ))
                )
            )
            entity.addToTempArray(message.messageId.toString())
        } catch (e: Exception) {
            ZixaMCTGBridge.logger.error(e.message)
            val botUsername = config.easyAuth.langMinecraft.botUsername
            player.sendMessage(
                MinecraftAdventureConverter.adventureToMinecraft(
                    Component.text(BotLogic.escapePlaceholders(config.easyAuth.langMinecraft.noHaveChatWithBot, botUsername))
                        .toBuilder()
                            .hoverEvent(Component.text(BotLogic.escapePlaceholders(config.chatSync.lang.minecraft.messageMeta.hoverOpenInTelegram, nickname)))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://t.me/${botUsername.replace("@", "")}"))
                        .build()
                )
            )
        }
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