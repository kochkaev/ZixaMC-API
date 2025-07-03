package ru.kochkaev.zixamc.tgbridge.telegram.easyAuth

import net.minecraft.server.network.ServerPlayerEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLUser
import ru.kochkaev.zixamc.tgbridge.sql.data.AccountType
import ru.kochkaev.zixamc.tgbridge.sql.data.MinecraftAccountType
import ru.kochkaev.zixamc.tgbridge.telegram.BotLogic
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.server
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup
import xyz.nikitacartes.easyauth.utils.PlayerAuth
import java.time.ZonedDateTime
import xyz.nikitacartes.easyauth.EasyAuth.config as easyAuthConfig


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

    suspend fun approve(entity: SQLUser, nickname: String) {
        val player = server.playerManager.getPlayer(nickname)
        val data = (player as PlayerAuth).`easyAuth$getPlayerEntryV1`()
        (player as PlayerAuth).`easyAuth$setAuthenticated`(true)
        (player as PlayerAuth).`easyAuth$restoreTrueLocation`()
        data.lastAuthenticatedDate = ZonedDateTime.now()
        data.loginTries = 0L
        player.sendMessage(config.easyAuth.langMinecraft.onApprove.getMinecraft())
        try {
            bot.sendMessage(
                chatId = entity.userId,
                text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.onApprove, nickname),
            )
        } catch (_: Exception) {}
    }
    suspend fun deny(entity: SQLUser, nickname: String) {
        val player = server.playerManager.getPlayer(nickname)
        val data = (player as PlayerAuth).`easyAuth$getPlayerEntryV1`()
        player.networkHandler.disconnect(config.easyAuth.langMinecraft.onDeny.getMinecraft())
        data.lastKickedDate = ZonedDateTime.now()
        data.loginTries = 0L
        try {
            bot.sendMessage(
                chatId = entity.userId,
                text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.onDeny, nickname),
            )
        } catch (_: Exception) {}
    }
    suspend fun onJoin(player: ServerPlayerEntity) {
        val data = (player as PlayerAuth).`easyAuth$getPlayerEntryV1`()
        if (!easyAuthConfig.enableGlobalPassword && (data.password.isEmpty())) return
        val nickname = player.nameForScoreboard
        val entity = SQLUser.get(nickname)
        if (entity == null || !entity.hasProtectedLevel(AccountType.PLAYER) || !entity.data.minecraftAccounts.fold(false) {
                acc, it -> acc || MinecraftAccountType.getAllActiveNow().contains(it.accountStatus) && it.nickname == nickname
            }) return kickYouAreNotPlayer(player)
        if ((player as PlayerAuth).`easyAuth$canSkipAuth`() || (player as PlayerAuth).`easyAuth$isAuthenticated`()) return
        player.sendMessage(config.easyAuth.langMinecraft.onJoinTip.getMinecraft())
        try {
            val message = bot.sendMessage(
                chatId = entity.userId,
                text = BotLogic.escapePlaceholders(config.easyAuth.langTelegram.onJoinTip, nickname),
                replyMarkup = TgInlineKeyboardMarkup(
                    listOf(
                        listOf(
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                text = BotLogic.escapePlaceholders(
                                    config.easyAuth.langTelegram.buttonApprove,
                                    nickname
                                ),
//                            callback_data = TgCallback("easyauth", EasyAuthCallbackData(nickname, "approve")).serialize()
                                callback_data = "easyauth\$approve/$nickname"
                            ),
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
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
        val entity = SQLUser.get(nickname)?:return
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
        val data = (player as PlayerAuth).`easyAuth$getPlayerEntryV1`()
        data.loginTries = 0L
        data.lastKickedDate = ZonedDateTime.now()
    }
}