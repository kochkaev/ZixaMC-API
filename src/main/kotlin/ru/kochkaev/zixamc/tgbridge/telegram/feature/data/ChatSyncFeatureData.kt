package ru.kochkaev.zixamc.tgbridge.telegram.feature.data

import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1
import net.kyori.adventure.text.Component
import ru.kochkaev.zixamc.api.config.TextData
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotCore
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotCore.config
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotLogic
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.MinecraftAdventureConverter
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser.replyToText
//import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser.topicToText
import ru.kochkaev.zixamc.api.telegram.model.TgMessage

class ChatSyncFeatureData (
    topicId: Int? = null,
    var enabled: Boolean = true,
//    val name: String? = null,
//    val aliases: ArrayList<String> = arrayListOf(),
    var prefix: TextData? = null,
    var fromMcPrefix: TextData? = null,
    group: SQLGroup? = null,
): TopicFeatureData(topicId, group) {
    fun getResolvedPrefix(messageId: Int): Component? = group?.let { group -> with(group) {
        config.lang.minecraft.prefixAppend.get(
            plainPlaceholders = listOf(
                "group" to name.toString(),
                "message_id" to messageId.toString()
            ),
            componentPlaceholders = listOf(
                "prefix" to (prefix?.get() ?: Component.text(name.toString()))
            )
        )
    } }
    fun getResolvedFromMcPrefix(messageId: Int): Component? = group?.let { group -> with(group) {
        config.lang.minecraft.prefixAppend.get(
            plainPlaceholders = listOf(
                "group" to name.toString(),
                "message_id" to messageId.toString()
            ),
            componentPlaceholders = listOf(
                "prefix" to (fromMcPrefix?.get() ?: prefix?.get() ?: Component.text(name.toString()))
            )
        )
    } }

    fun checkValidMsg(msg: TgMessage) = msg.let {
        group?.enabled?:false &&
        enabled &&
        topicId?.let { topic -> it.messageThreadId == topic } ?: true
    }

    suspend fun broadcastMinecraft(
        nickname: String,
        message: String,
        replyTo: Int? = null,
    ): BroadcastMinecraftResult = group?.let { group -> with(group) {
        if (!enabled || !isMember(nickname)) return@with BroadcastMinecraftResult.NOT_FOUND
        val tgMessage = try {
            ChatSyncBotLogic.sendReply(message, this, nickname, replyTo)
        } catch (e: Exception) { return@with BroadcastMinecraftResult.MESSAGE_NOT_FOUND }
        if (tgMessage != null) {
            val messages = mutableListOf<Component>()
            val components = mutableListOf<Component>()
//            topicToText(tgMessage)?.also { components.add(it) }
            replyToText(tgMessage, topicId, TextParser.resolveMessageLink(tgMessage), bot.me.id)?.also {
                if (!config.messages.replyInDifferentLine) components.add(it)
                else messages.add(it).also { messages.add(Component.text("\n")) }
            }
            components.add(MinecraftAdventureConverter.minecraftToAdventure(
                MarkdownLiteParserV1.ALL.parseNode(message).toText()
            ))
            messages.add(config.lang.minecraft.messageMCFormat.get(
                plainPlaceholders = listOf(
                    "nickname" to nickname,
                ),
                componentPlaceholders = listOf(
                    "text" to components
                        .flatMap { component -> listOf(component, Component.text(" ")) }
                        .fold(Component.text()) { acc, it -> acc.append(it) }
                        .build(),
                    "prefix" to getResolvedFromMcPrefix(tgMessage.messageId)!!,
                )
            ))
            ChatSyncBotCore.broadcastMessage(
                messages
                    .fold(Component.text()) { acc, component -> acc.append(component) }
                    .build(),
                this
            )
            BroadcastMinecraftResult.SUCCESS
        } else BroadcastMinecraftResult.TG_ERROR
    } } ?: BroadcastMinecraftResult.TG_ERROR
    enum class BroadcastMinecraftResult {
        SUCCESS,
        NOT_FOUND,
        MESSAGE_NOT_FOUND,
        TG_ERROR,
    }
}
