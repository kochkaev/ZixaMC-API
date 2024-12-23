package ru.kochkaev.zixamc.tgbridge.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import ru.kochkaev.zixamc.tgbridge.MySQLIntegration


object ZixaMCCommand {
    fun registerCommand(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        dispatcher.register(
            CommandManager.literal("zixamc")
                .requires { it.hasPermissionLevel(2) }
                .then(CommandManager.literal("promote")
                    .then(CommandManager.argument("user_id", LongArgumentType.longArg())
                        .suggests(UserIDSuggestionProvider())
                        .then(CommandManager.argument("account_type", StringArgumentType.string())
                            .suggests(AccountTypesSuggestionProvider())
                            .executes { context ->
                                val userId = context.getArgument("user_id", Long::class.java)
                                val accountType = context.getArgument("account_type", String::class.java)
                                MySQLIntegration.getLinkedEntity(userId)!!
                                    .accountType = when (accountType.lowercase()) {
                                        "admin" -> 0
                                        "player" -> 1
                                        "requester" -> 2
                                        else -> 3
                                    }
                                context.source.sendFeedback( { Text.literal("Successfully promoted user $userId to $accountType") }, true)
                                0
                            }
                        )
                    )
                )
        )
    }
}