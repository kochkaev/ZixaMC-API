package ru.kochkaev.zixamc.tgbridge.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import ru.kochkaev.zixamc.tgbridge.ServerBot
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore.config
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotLogic
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.MinecraftAdventureConverter
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser.replyToText
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup


object ReplyCommand {
    private val replyThen = CommandManager.argument("group", StringArgumentType.word())
        .suggests(GroupNameSuggestionProvider())
        .then(CommandManager.argument("message_id", LongArgumentType.longArg())
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
        if (group == null || !group.isMember(context.source.name)) {
            context.source.sendFeedback(
                { ServerBot.config.chatSync.reply.minecraftCommand.chatNotFound.getMinecraft(
                    listOf("group" to groupName)
                ) }, false
            )
            return@runBlocking 0
        }
        val messageId =
            if (withReply) LongArgumentType.getLong(context, "message_id")
            else null
        val message = StringArgumentType.getString(context, "message")
        val tgMessage = ChatSyncBotLogic.sendReply(message, group, context.source.name, messageId)
        if (tgMessage != null) {
            val messages = mutableListOf<Component>()
            var mcMessage = message
            replyToText(tgMessage, group.topicId, ServerBot.bot.me.id)?.also {
                if (!config.messages.replyInDifferentLine) mcMessage = "$it $mcMessage"
                else messages.add(it).also { messages.add(Component.text("\n")) }
            }
            messages.add(
                config.reply.minecraftCommand.minecraftSchema.get(
                    plainPlaceholders = listOf(
                        "nickname" to context.source.name,
                    ),
                    componentPlaceholders = listOf(
                        "text" to MinecraftAdventureConverter.minecraftToAdventure(
                            MarkdownLiteParserV1.ALL.parseNode(mcMessage).toText()
                        ),
                        "prefix" to group.getResolvedFromMcPrefix(tgMessage.messageId),
                    )
                )
            )
            ChatSyncBotCore.broadcastMessage(
                messages
                    .fold(Component.text()) { acc, component -> acc.append(component) }
                    .build(),
                group
            )
        } else context.source.sendFeedback(
            { ServerBot.config.chatSync.reply.minecraftCommand.errorDueSending.getMinecraft() },
            false
        )
        0
    }

//    fun decodeGroupId(group: String): Long? =
//        if (group.lowercase() == "zixa")
//            config.chatId
//        else null
//    fun decodeGroupPrefix(group: String, messageId: Long): Component? =
//        if (group.lowercase() == "zixa")
//            config.reply.defaultPrefix.get(listOf("message_id" to messageId.toString()))
//        else null
}