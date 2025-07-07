package ru.kochkaev.zixamc.easyauthintegration

import com.google.gson.annotations.SerializedName
import net.minecraft.server.network.ServerPlayerEntity
import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.callback.CallbackData
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataTypes
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.data.MinecraftAccountType
import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.easyauthintegration.Config.Companion.config
import ru.kochkaev.zixamc.api.telegram.ServerBot.server
import ru.kochkaev.zixamc.api.telegram.model.TgCallbackQuery
import ru.kochkaev.zixamc.api.telegram.model.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.api.telegram.model.TgReplyMarkup
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
        player.sendMessage(config.langMinecraft.onApprove.getMinecraft())
        try {
            bot.sendMessage(
                chatId = entity.userId,
                text = config.langTelegram.onApprove.formatLang("nickname" to nickname),
            )
        } catch (_: Exception) {}
    }
    suspend fun deny(entity: SQLUser, nickname: String) {
        val player = server.playerManager.getPlayer(nickname)
        val data = (player as PlayerAuth).`easyAuth$getPlayerEntryV1`()
        player.networkHandler.disconnect(config.langMinecraft.onDeny.getMinecraft())
        data.lastKickedDate = ZonedDateTime.now()
        data.loginTries = 0L
        try {
            bot.sendMessage(
                chatId = entity.userId,
                text = config.langTelegram.onDeny.formatLang("nickname" to nickname),
            )
        } catch (_: Exception) {}
    }
    suspend fun onJoin(player: ServerPlayerEntity) {
        val data = (player as PlayerAuth).`easyAuth$getPlayerEntryV1`()
        if (!easyAuthConfig.enableGlobalPassword && (data.password.isEmpty())) return
        val nickname = player.nameForScoreboard
        val entity = SQLUser.get(nickname)
        if (entity == null || !entity.hasProtectedLevel(AccountType.PLAYER) || entity.data.getCasted(ChatDataTypes.MINECRAFT_ACCOUNTS)?.fold(false) {
                acc, it -> acc || MinecraftAccountType.getAllActiveNow().contains(it.accountStatus) && it.nickname == nickname
            } != true) return kickYouAreNotPlayer(player)
        if ((player as PlayerAuth).`easyAuth$canSkipAuth`() || (player as PlayerAuth).`easyAuth$isAuthenticated`()) return
        player.sendMessage(config.langMinecraft.onJoinTip.getMinecraft())
        try {
            val message = bot.sendMessage(
                chatId = entity.userId,
                text = config.langTelegram.onJoinTip.formatLang("nickname" to nickname),
                replyMarkup = TgInlineKeyboardMarkup(
                    listOf(
                        listOf(
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                text = config.langTelegram.buttonApprove.formatLang("nickname" to nickname),
//                            callback_data = TgCallback("easyauth", EasyAuthCallbackData(nickname, "approve")).serialize()
                                callback_data = $$"easyauth$approve/$$nickname"
                            ),
                            TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                                text = config.langTelegram.buttonDeny.formatLang("nickname" to nickname),
//                            callback_data = TgCallback("easyauth", EasyAuthCallbackData(nickname, "deny")).serialize()
                                callback_data = $$"easyauth$deny/$$nickname"
                            ),
                        )
                    )
                )
            )
            entity.tempArray.add(message.messageId.toString())
        } catch (e: Exception) {
            val botUsername = config.langMinecraft.botUsername
            player.sendMessage(
                config.langMinecraft.noHaveChatWithBot.getMinecraft(
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
                    replyMarkup = TgReplyMarkup(),
                )
            } catch (_: Exception) {}
        }
        entity.tempArray.set(listOf())
    }
    private fun kickYouAreNotPlayer(player: ServerPlayerEntity?) {
        player?.networkHandler?.disconnect(config.langMinecraft.youAreNotPlayer.getMinecraft())
        val data = (player as PlayerAuth).`easyAuth$getPlayerEntryV1`()
        data.loginTries = 0L
        data.lastKickedDate = ZonedDateTime.now()
    }

    suspend fun onTelegramCallbackQuery(cbq: TgCallbackQuery, /*data: TgCallback<EasyAuthCallbackData>*/) {
//        val args = cbq.data?.split(Regex("easyauth\$(.*?)/([a-zA-Z0-9_])"))
        val data = cbq.data?:return
        if (!cbq.data.startsWith("easyauth")) return
        val args = data.substring(data.indexOf('\$')+1, data.length)
        val nickname = args.substring(args.indexOf('/')+1, args.length)
        val operation = args.substring(0, args.indexOf('/'))
        val user = SQLUser.get(cbq.from.id)?:return
        when (/*data.data!!.operation*/ operation) {
            "approve" -> approve(user, /*data.data.nickname*/nickname)
            "deny" -> deny(user, /*data.data.nickname*/nickname)
        }
        user.tempArray.remove(cbq.message.messageId.toString()).toString()
        bot.editMessageReplyMarkup(
            chatId = cbq.message.chat.id,
            messageId = cbq.message.messageId,
            replyMarkup = TgReplyMarkup(),
        )
    }

    data class EasyAuthCallbackData(
        @SerializedName("n")
        val nickname: String,
        @SerializedName("o")
        val operation: String
    ) : CallbackData
}