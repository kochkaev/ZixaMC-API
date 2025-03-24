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
        val messageId =
            if (withReply) IntegerArgumentType.getInteger(context, "message_id")
            else null
        val message = StringArgumentType.getString(context, "message")
        val result = group?.broadcastMinecraft(context.source.name, message, messageId) ?: SQLGroup.BroadcastMinecraftResult.NOT_FOUND
        when (result) {
            SQLGroup.BroadcastMinecraftResult.NOT_FOUND ->
                context.source.sendFeedback(
                    { ServerBot.config.chatSync.reply.chatNotFound.getMinecraft(
                        listOf("group" to groupName)
                    ) }, false
                )
            SQLGroup.BroadcastMinecraftResult.TG_ERROR ->
                context.source.sendFeedback(
                    { ServerBot.config.chatSync.reply.errorDueSending.getMinecraft() },
                    false
                )
            else -> {}
        }
        0
    }
}