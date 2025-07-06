package ru.kochkaev.zixamc.chatsync

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.text.Component
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedMessage
import net.minecraft.server.command.CommandManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import ru.kochkaev.zixamc.api.telegram.ServerBot
import ru.kochkaev.zixamc.api.telegram.ServerBot.server
import ru.kochkaev.zixamc.chatsync.parser.MinecraftAdventureConverter
import ru.kochkaev.zixamc.easyauthintegration.EasyAuthCustomEvents
import ru.kochkaev.zixamc.easyauthintegration.EasyAuthIntegration
import ru.kochkaev.zixamc.api.sql.SQLGroup


object ChatSyncBotCore {

    var vanishInstance: FabricVanishIntegration? = if (FabricLoader.getInstance().isModLoaded("melius-vanish")) FabricVanishIntegration else null
    val config: Config
        get() = Config.config
    val lang: Config.ChatSyncLang
        get() = Config.config.lang
    fun registerChatMessageListener(handler: (TBPlayerEventData) -> Boolean) {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register { message: SignedMessage, sender, params ->
            if (
                params.type.matchesKey(MessageType.CHAT) &&
                sender is ServerPlayerEntity
                && (vanishInstance == null || !FabricVanishIntegration.isVanished(sender))
                && EasyAuthIntegration.isAuthenticated(sender)
            ) {
                handler.invoke(
                    TBPlayerEventData(
                        sender.displayName?.string ?: return@register true,
                        Component.text(message.signedBody.content),
                    )
                )
            } else true
        }
//        else styledChatInstance.registerMessageEvent(handler)
    }
    fun registerSayMessageListener(handler: (TBPlayerEventData) -> Unit) {
        ServerMessageEvents.COMMAND_MESSAGE.register { message: SignedMessage, sender, params ->
            if (
                params.type.matchesKey(MessageType.SAY_COMMAND) &&
                ((sender.player != null
                && (vanishInstance == null || !FabricVanishIntegration.isVanished(sender.player!!))
                && EasyAuthIntegration.isAuthenticated(sender.player!!))
                || sender.player == null)
            ) {
                handler.invoke(
                    TBPlayerEventData(
                        sender.displayName?.string ?: return@register,
                        Component.text(message.signedBody.content),
                    )
                )
            }
        }
//        else styledChatInstance.registerMessageEvent(handler)
    }
    fun registerMeMessageListener(handler: (TBPlayerEventData) -> Unit) {
        ServerMessageEvents.COMMAND_MESSAGE.register { message: SignedMessage, sender, params ->
            if (
                params.type.matchesKey(MessageType.EMOTE_COMMAND) &&
                ((sender.player != null
                && (vanishInstance == null || !FabricVanishIntegration.isVanished(sender.player!!))
                && EasyAuthIntegration.isAuthenticated(sender.player!!))
                || sender.player == null)
            ) {
                handler.invoke(
                    TBPlayerEventData(
                        sender.displayName?.string ?: return@register,
                        Component.text(message.signedBody.content),
                    )
                )
            }
        }
//        else styledChatInstance.registerMessageEvent(handler)
    }

    fun registerPlayerDeathListener(handler: (TBPlayerEventData) -> Unit) {
        ChatSyncCustomEvents.PLAYER_DIE_EVENT.register { entity, deathMessage ->
            if (vanishInstance == null || !FabricVanishIntegration.isVanished(entity)) {
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
            val player = handlr.player
            if (
                EasyAuthIntegration.isAuthenticated(player)
                && (vanishInstance == null || !FabricVanishIntegration.isVanished(player))
            ) handler.invoke(
                TBPlayerEventData(
                    player.displayName?.string ?: return@register,
                    Component.text(""),
                )
            )
        }
        if (EasyAuthIntegration.isEnabled)
            EasyAuthCustomEvents.UPDATE_PLAYER_AUTHENTICATED_EVENT.register { authenticated, player ->
                if (authenticated && ServerBot.config.easyAuth.suppressMessagesWithoutAuth) {
                    EasyAuthIntegration.addToPrevious(player)
                    if (vanishInstance == null || !FabricVanishIntegration.isVanished(player)) handler.invoke(
                        TBPlayerEventData(
                            player.displayName?.string ?: return@register,
                            Component.text(""),
                        )
                    )
                }
            }
        FabricVanishIntegration.registerOnJoinMessage(handler)
    }

    fun registerPlayerLeaveListener(handler: (TBPlayerEventData) -> Unit) {
        ServerPlayConnectionEvents.DISCONNECT.register { handlr, _ ->
            val player = handlr.player
            if (
                EasyAuthIntegration.isAuthenticated(player)
                && (vanishInstance == null || !FabricVanishIntegration.isVanished(player))
            ) handler.invoke(
                TBPlayerEventData(
                    player.displayName?.string ?: return@register,
                    Component.text(""),
                )
            )
        }
        if (EasyAuthIntegration.isEnabled)
            EasyAuthCustomEvents.UPDATE_PLAYER_AUTHENTICATED_EVENT.register { authenticated, player ->
                if (!authenticated && ServerBot.config.easyAuth.suppressMessagesWithoutAuth)
                    if (
                        (EasyAuthIntegration.isAuthenticated(player) && !player.isDisconnected)
                        && (vanishInstance == null || !FabricVanishIntegration.isVanished(player))
                    ) handler.invoke(
                        TBPlayerEventData(
                            player.displayName?.string ?: return@register,
                            Component.text(""),
                        )
                    )
            }
        FabricVanishIntegration.registerOnLeaveMessage(handler)
    }

    fun registerPlayerAdvancementListener(handler: (TBPlayerEventData) -> Unit) {
        ChatSyncCustomEvents.ADVANCEMENT_EARN_EVENT.register { player, advancementType, advancementNameComponent ->
            if (vanishInstance == null || !FabricVanishIntegration.isVanished(player)) {
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
                    val res = handler(
                        TBCommandContext(
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

    fun broadcastMessage(text: Component, group: SQLGroup) {
        server.playerManager.playerList
            .filter { group.isMember(it.nameForScoreboard) }
            .forEach { it.sendMessage(MinecraftAdventureConverter.adventureToMinecraft(text), false) }
    }

    fun getOnlinePlayerNames(): Array<String> {
        var players = server.playerManager.playerList
        if (vanishInstance !=null) players = players.filter {
            !FabricVanishIntegration.isVanished(it)
        }
        return players.map { it.nameForScoreboard }.toTypedArray()
    }
}