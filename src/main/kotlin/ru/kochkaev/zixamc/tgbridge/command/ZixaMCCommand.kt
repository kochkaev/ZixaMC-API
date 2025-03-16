package ru.kochkaev.zixamc.tgbridge.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.AccountType


object ZixaMCCommand {
    fun     registerCommand(dispatcher: CommandDispatcher<ServerCommandSource?>) {
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
                                SQLEntity.get(userId)
                                    ?.accountType = AccountType.parse(accountType.lowercase())
                                context.source.sendFeedback( { Text.literal("Successfully promoted user $userId to $accountType") }, true)
                                0
                            }
                        )
                    )
                )
                .then(CommandManager.literal("reload")
                    .executes { context ->
                        ConfigManager.load()
                        context.source.sendFeedback({ Text.of("§7ZixaMCTGBridge configs successfully reloaded!") }, true)
                        0
                    }
                )
                .then(CommandManager.literal("silentRestart")
                    .executes { context ->
                        ZixaMCTGBridge.tmp.isSilentRestart = true
                        ConfigManager.update()
                        context.source.sendFeedback({ Text.of("Server restart will not be seen in telegram.") }, true)
                        context.source.server.shutdown()
                        0
                    }
                )
        )
    }
}