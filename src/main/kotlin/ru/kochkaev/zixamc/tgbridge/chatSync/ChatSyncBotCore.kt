package ru.kochkaev.zixamc.tgbridge.chatSync

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.Component
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.command.CommandManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import ru.kochkaev.zixamc.tgbridge.Config
import ru.kochkaev.zixamc.tgbridge.ConfigManager
import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.ServerBot.server
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.MinecraftAdventureConverter
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgEntity
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage


object ChatSyncBotCore {

    var vanishInstance: FabricVanishIntegration? = if (FabricLoader.getInstance().isModLoaded("melius-vanish")) FabricVanishIntegration else null
    lateinit var config: Config.ServerBotDataClass.ServerBotChatSyncDataClass
    lateinit var lang: Config.ServerBotDataClass.ServerBotChatSyncDataClass.ServerBotChatSyncLangDataClass
    fun init() {
        config = ConfigManager.CONFIG!!.serverBot.chatSync
        lang = ConfigManager.CONFIG!!.serverBot.chatSync.lang
        ChatSyncBotLogic.registerTelegramHandlers()
        ChatSyncBotLogic.registerMinecraftHandlers()
    }
    fun registerChatMessageListener(handler: (TBPlayerEventData) -> Unit) {
        ServerMessageEvents.CHAT_MESSAGE.register { message: SignedMessage, sender, _ ->
            if (sender is ServerPlayerEntity && (vanishInstance == null || !vanishInstance!!.isVanished(sender))) {
                handler.invoke(
                    TBPlayerEventData(
                        sender.displayName?.string ?: return@register,
                        MinecraftAdventureConverter.minecraftToAdventure(message.content),
                    )
                )
            }
        }
//        else styledChatInstance.registerMessageEvent(handler)
    }

    fun registerPlayerDeathListener(handler: (TBPlayerEventData) -> Unit) {
        ServerLivingEntityEvents.AFTER_DEATH.register { entity, damageSource ->
            if (entity is ServerPlayerEntity && (vanishInstance == null || !vanishInstance!!.isVanished(entity))) {
                val deathMessage = damageSource.getDeathMessage(entity)
                handler(
                    TBPlayerEventData(
                        entity.displayName?.string ?: return@register,
                        MinecraftAdventureConverter.minecraftToAdventure(deathMessage),
                    )
                )
            }
        }
    }

    fun registerPlayerJoinListener(handler: (TBPlayerEventData) -> Unit) {
        ServerPlayConnectionEvents.JOIN.register { handlr, _, _ ->
            if (vanishInstance == null || !vanishInstance!!.isVanished(handlr.player))handler.invoke(
                TBPlayerEventData(
                    handlr.player.displayName?.string ?: return@register,
                    Component.text(""),
                )
            )
        }
        vanishInstance?.registerOnJoinMessage(handler)
    }

    fun registerPlayerLeaveListener(handler: (TBPlayerEventData) -> Unit) {
        ServerPlayConnectionEvents.DISCONNECT.register { handlr, _ ->
            if (vanishInstance == null || !vanishInstance!!.isVanished(handlr.player))handler.invoke(
                TBPlayerEventData(
                    handlr.player.displayName?.string ?: return@register,
                    Component.text(""),
                )
            )
        }
        vanishInstance?.registerOnLeaveMessage(handler)
    }

    fun registerPlayerAdvancementListener(handler: (TBPlayerEventData) -> Unit) {
        CustomEvents.ADVANCEMENT_EARN_EVENT.register { player, advancementType, advancementNameComponent ->
            if (vanishInstance == null || !vanishInstance!!.isVanished(player)) {
                if (player.displayName == null) {
                    return@register
                }
                val advancementTypeKey = "chat.type.advancement.$advancementType"
                val advancementText =
                    Text.translatable(advancementTypeKey, player.displayName, advancementNameComponent)
                handler(
                    TBPlayerEventData(
                        player.displayName!!.string,
                        MinecraftAdventureConverter.minecraftToAdventure(advancementText)
                    )
                )
            }
        }
    }

    fun registerCommand(command: Array<String>, handler: (TBCommandContext) -> Boolean) {
        // TODO: get rid of code duplication between versions and loaders
        val builder = CommandManager.literal(command[0])
        var lastArg = builder
        command.drop(1).forEachIndexed { i, x ->
            val newArg = CommandManager.literal(x)
            if (i == command.size - 2) {
                newArg.executes { ctx ->
                    val res = handler(TBCommandContext(
                        reply = { text ->
                            val textComponent = Text.literal(text)
                            ctx.source.sendFeedback({ textComponent }, false)
                        }
                    ))
                    return@executes if (res) 1 else -1
                }
            }
            lastArg.then(newArg)
            lastArg = newArg
        }
        server.commandManager.dispatcher.register(builder)
    }

    fun broadcastMessage(text: Component) {
        server.playerManager.broadcast(MinecraftAdventureConverter.adventureToMinecraft(text), false)
    }

    fun getOnlinePlayerNames(): Array<String> {
        return server.playerNames
    }

    suspend fun sendMessage(text: String, entities: List<TgEntity>? = null): TgMessage {
        return bot.sendMessage(config.chatId, text, messageThreadId = config.topicId, entities=entities)
    }
    suspend fun editMessageText(messageId: Int, text: String, entities: List<TgEntity>? = null): TgMessage {
        return bot.editMessageText(config.chatId, messageId, text, entities=entities)
    }
    suspend fun deleteMessage(messageId: Int) {
        bot.deleteMessage(config.chatId, messageId)
    }
}