package ru.kochkaev.zixamc.tgbridge.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.runBlocking
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureTypes
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.ChatSyncFeatureData


object ReplyCommand {
    private val replyThen = CommandManager.argument("group", StringArgumentType.word())
        .suggests(GroupNameSuggestionProvider())
        .then(CommandManager.argument("message_id", IntegerArgumentType.integer())
            .then(CommandManager.argument("message", StringArgumentType.greedyString())
                .executes { context -> sendReply(context, true) }
            )
        )
        .then(CommandManager.argument("message", StringArgumentType.greedyString())
            .executes { context -> sendReply(context, false) }
        )
    fun registerCommand(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("reply").then(replyThen)
        )
        dispatcher.register(
            CommandManager.literal("r").then(replyThen)
        )
    }
    private fun sendReply(context: CommandContext<ServerCommandSource>, withReply: Boolean) = runBlocking {
        val groupName = StringArgumentType.getString(context, "group")
        val group = SQLGroup.get(groupName)
//        if (group == null) {
//            context.source.sendFeedback({ ServerBot.config.integration.group.groupNotFound.getMinecraft(listOf("group" to groupName)) }, false)
//        }
        val messageId =
            if (withReply) IntegerArgumentType.getInteger(context, "message_id")
            else null
        val message = StringArgumentType.getString(context, "message")
        val result = group?.features?.getCasted(FeatureTypes.CHAT_SYNC)?.broadcastMinecraft(context.source.name, message, messageId) ?: ChatSyncFeatureData.BroadcastMinecraftResult.NOT_FOUND
        when (result) {
            ChatSyncFeatureData.BroadcastMinecraftResult.NOT_FOUND ->
                context.source.sendFeedback(
                    { ServerBot.config.chatSync.reply.chatNotFound.getMinecraft(
                        listOf("group" to groupName)
                    ) }, false
                )
            ChatSyncFeatureData.BroadcastMinecraftResult.MESSAGE_NOT_FOUND ->
                context.source.sendFeedback(
                    { ServerBot.config.chatSync.reply.messageIdNotFound.getMinecraft(
                        listOf("group" to groupName, "messageId" to messageId.toString())
                    ) }, false
                )
            ChatSyncFeatureData.BroadcastMinecraftResult.TG_ERROR ->
                context.source.sendFeedback(
                    { ServerBot.config.chatSync.reply.errorDueSending.getMinecraft() },
                    false
                )
            else -> {}
        }
        0
    }
}