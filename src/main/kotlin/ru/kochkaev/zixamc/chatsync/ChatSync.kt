package ru.kochkaev.zixamc.chatsync

import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import ru.kochkaev.zixamc.api.telegram.ServerBot
import ru.kochkaev.zixamc.chatsync.command.ReplyCommand

class ChatSync: ModInitializer {

    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped)
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            ReplyCommand.registerCommand(dispatcher)
        }
    }
    fun onServerStopped(server: MinecraftServer) {
        runBlocking {
            if (ServerBot.config.chatSync.isEnabled) ChatSyncBotLogic.sendServerStoppedMessage()
        }
    }

}