package ru.kochkaev.zixamc.tgbridge.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
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
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser.replyToText
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup


object ReplyCommand {
    private val replyThen = CommandManager.argument("group", StringArgumentType.word())
        .then(CommandManager.argument("message_id", LongArgumentType.longArg())
            .then(CommandManager.argument("message", StringArgumentType.greedyString())
                .executes { context ->
                    runBlocking {
                        val groupName = StringArgumentType.getString(context, "group")
                        val group = SQLGroup.get(groupName)
                        if (group == null) {
                            context.source.sendFeedback(
                                { ServerBot.config.chatSync.reply.minecraftCommand.chatNotFound.getMinecraft(
                                    listOf("group" to groupName)
                                ) }, false
                            )
                            return@runBlocking 0
                        }
                        val messageId = LongArgumentType.getLong(context, "message_id")
                        val message = StringArgumentType.getString(context, "message")
                        val tgMessage = ChatSyncBotLogic.sendReply(message, context.source.name, group.chatId, messageId)
                        if (tgMessage != null) {
                            var mcMessage = message
                            replyToText(tgMessage, ServerBot.bot.me.id)?.also {
                                if (!config.messages.replyInDifferentLine) mcMessage = "$it $mcMessage"
                                else ChatSyncBotCore.broadcastMessage(it)
                            }
                            context.source.sendMessage(
                                config.reply.minecraftCommand.minecraftSchema.getMinecraft(
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
                        } else context.source.sendFeedback(
                            { ServerBot.config.chatSync.reply.minecraftCommand.errorDueSending.getMinecraft() },
                            false
                        )
                        0
                    }
                }
            )
        )
    fun registerCommand(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("reply").then(replyThen)
        )
        dispatcher.register(
            CommandManager.literal("r").then(replyThen)
        )
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