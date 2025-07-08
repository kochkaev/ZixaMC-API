package ru.kochkaev.zixamc.chatsync

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager
import net.minecraft.text.Text
import ru.kochkaev.zixamc.api.Initializer
import ru.kochkaev.zixamc.api.ZixaMC
import ru.kochkaev.zixamc.api.command.ZixaMCCommand
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.config.TempConfig
import ru.kochkaev.zixamc.api.config.TempConfigValueType
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataTypes
import ru.kochkaev.zixamc.api.sql.feature.FeatureTypes
import ru.kochkaev.zixamc.api.sql.feature.data.PlayersGroupFeatureData
import ru.kochkaev.zixamc.api.telegram.ServerBot
import ru.kochkaev.zixamc.chatsync.command.ReplyCommand
import ru.kochkaev.zixamc.chatsync.settings.ChatSyncFeatureData
import ru.kochkaev.zixamc.chatsync.settings.ChatSyncFeatureType

class ChatSync: ModInitializer {

    companion object {
        private val isSilentRestartTempValueType = TempConfigValueType.of($$"chatsync$silentRestart", Boolean::class.java)
        var isSilentRestart: Boolean
            get() = TempConfig.temp.get(isSilentRestartTempValueType) ?: false
            set(isSilentRestart) {
                if (isSilentRestart) TempConfig.temp.set(isSilentRestartTempValueType, true)
                else TempConfig.temp.remove(isSilentRestartTempValueType)
            }
    }

    override fun onInitialize() {
        TempConfig.registerValueType(isSilentRestartTempValueType)
        if (!SQLGroup.exists("main")) createMainGroup()
        ChatSyncBotLogic.registerTelegramHandlers()
        ChatSyncBotLogic.registerMinecraftHandlers()
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping)
        ZixaMCCommand.registerIntegration(CommandManager.literal("silentRestart")
            .executes { context ->
                isSilentRestart = true
                ru.kochkaev.zixamc.api.config.Config.update()
                context.source.sendFeedback({ Text.of("Server restart will not be seen in telegram.") }, true)
                context.source.server.stop(false)
                0
            }
        )
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            ReplyCommand.registerCommand(dispatcher)
        }
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            Initializer.coroutineScope.launch {
                ChatSyncBotLogic.sendServerStartedMessage()
            }
        }
    }
    fun onServerStopping(server: MinecraftServer) {
        runBlocking {
            ChatSyncBotLogic.sendServerStoppedMessage()
        }
    }
    fun createMainGroup() {
        val config = Config.config
        SQLGroup.create(
            chatId = config.defaultGroup.chatId,
            name = config.defaultGroup.name,
            aliases = config.defaultGroup.aliases,
            members = SQLUser.users.map { it.id.toString() },
            agreedWithRules = true,
            isRestricted = false,
            features = mapOf(
                ChatSyncFeatureType to ChatSyncFeatureData(
                    enabled = true,
                    topicId = config.defaultGroup.topicId,
//                        name = config.defaultGroup.name,
//                        aliases = ArrayList(config.defaultGroup.aliases),
                    prefix = config.defaultGroup.prefix,
                    fromMcPrefix = config.defaultGroup.fromMcPrefix,
                    group = null,
                ),
                FeatureTypes.PLAYERS_GROUP to PlayersGroupFeatureData(
                    autoAccept = true,
                    autoRemove = true,
                    group = null,
                )
            ),
            data = mapOf(
                ChatDataTypes.IS_PRIVATE to true,
                ChatDataTypes.GREETING_ENABLE to false,
            )
        )
    }

}