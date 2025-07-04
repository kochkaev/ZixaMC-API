package ru.kochkaev.zixamc.api.command

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.ServerCommandSource
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureTypes
import java.util.concurrent.CompletableFuture


class GroupNameSuggestionProvider : SuggestionProvider<ServerCommandSource?> {
    @Throws(CommandSyntaxException::class)
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource?>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        SQLGroup.getAllWithFeature(FeatureTypes.CHAT_SYNC)
            .map { it.getSQLAssert() }
            .filter { it.enabled && it.isMember(context.source?.name?:"") }
            .forEach { builder.suggest(it.name) }
        return builder.buildFuture()
    }
}