package ru.kochkaev.zixamc.tgbridge.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.ServerCommandSource
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.TopicTypes
import java.util.concurrent.CompletableFuture


class GroupNameSuggestionProvider : SuggestionProvider<ServerCommandSource?> {
    @Throws(CommandSyntaxException::class)
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource?>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        SQLGroup.groups
            .map { it.getSQLAssert() }
            .filter { it.enabled && it.enabledChatSync && it.isMember(context.source?.name?:"") }
            .forEach { builder.suggest(it.name) }
        return builder.buildFuture()
    }
}