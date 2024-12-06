package ru.kochkaev.zixamc.tgbridge.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture


class AccountTypesSuggestionProvider : SuggestionProvider<ServerCommandSource?> {
    @Throws(CommandSyntaxException::class)
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource?>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        listOf("Admin", "Player", "Requester", "Unknown").forEach { builder.suggest(it) }
        return builder.buildFuture()
    }
}