package ru.kochkaev.zixamc.tgbridge

import java.util.*
import net.kyori.adventure.text.format.TextDecoration


/**
 * @author kochkaev
 */
data class Config (
    val mySQL: MySQLDataClass = MySQLDataClass(),
    val requestsBot: RequestsBotDataClass = RequestsBotDataClass(),
    val serverBot: ServerBotDataClass = ServerBotDataClass(),
) {
    data class MySQLDataClass (
        val mySQLHost: String = "",
        val mySQLDatabase: String = "",
        val mySQLUser: String = "",
        val mySQLPassword: String = "",
        val mySQLTable: String = "",
    )
    data class RequestsBotDataClass (
        val botToken: String = "",
        val botAPIURL: String = "https://api.telegram.org",
        val pollTimeout: Int = 60,
        val targetChatId: Long = 0,
        val targetTopicId: Int = 0,
        val playersGroupInviteLink: String = "https:/t.me/",
        val addWhitelistCommand: String = "easywhitelist add {nickname}",
        val removeWhitelistCommand: String = "easywhitelist add {nickname}",
        val poll: RequestsBotPollDataClass = RequestsBotPollDataClass(),
        val text: RequestsBotTextDataClass = RequestsBotTextDataClass(),
    ) {
        data class RequestsBotPollDataClass (
            val autoCreatePoll: Boolean = false,
            val pollQuestion: String = "",
            val pollAnswerTrue: String = "",
            val pollAnswerNull: String = "",
            val pollAnswerFalse: String = "",
        )
        data class RequestsBotTextDataClass (
            val textOnSend4User: String = "",
            val textOnSend4Target: String = "",
            val textSyntaxAcceptHelp: String = "",
            val textOnAccept4User: String = "",
            val textOnAccept4Target: String = "",
            val textSyntaxRejectHelp: String = "",
            val textOnReject4User: String = "",
            val textOnReject4Target: String = "",
            val textSyntaxPromoteHelp: String = "",
            val textOnPromote4Target: String = "",
            val textSyntaxKickHelp: String = "",
            val textOnKick4Target: String = "",
            val textOnKick4User: String = "",
            val textOnStart: String = "",
            val textButtonCreateRequest: String = "",
            val textNeedAgreeWithRules: String = "",
            val textMustAgreeWithRules: String = "",
            val textButtonAgreeWithRules: String = "",
            val textNeedNickname: String = "",
            val textInputFieldPlaceholderNickname: String = "",
            val textWrongNickname: String = "",
            val textTakenNickname: String = "",
            val textOnNewRequest: String = "",
            val textInputFieldPlaceholderRequest: String = "",
            val textConfirmSendRequest: String = "",
            val textButtonConfirmSending: String = "",
            val textYouAreNowCreatingRequest: String = "",
            val textButtonRedrawRequest: String = "",
            val textYouHavePendingRequest: String = "",
            val textCancelRequest: String = "",
            val textButtonCancelRequest: String = "",
            val textRequestCanceled4User: String = "",
            val textRequestCanceled4Target: String = "",
            val textYouAreNowPlayer: String = "",
            val textButtonJoinToPlayersGroup: String = "",
        )
    }
    data class ServerBotDataClass (
        val botToken: String = "",
        val botAPIURL: String = "https://api.telegram.org",
        val pollTimeout: Int = 60,
        val targetChatId: Long = 0,
        val targetTopicId: Int = 0,
        val chatSync: ServerBotChatSyncDataClass = ServerBotChatSyncDataClass(),
    ) {
        data class ServerBotChatSyncDataClass (
            val lang: ServerBotChatSyncLangDataClass = ServerBotChatSyncLangDataClass(),
        ) {
            data class ServerBotChatSyncLangDataClass (
                val telegram: LangTelegram = LangTelegram(),
                val minecraft: LangMinecraft = LangMinecraft()
            ) {
                data class LangAdvancements(
                    val regular: String = "😼 **{username} has made the advancement {title}**\n\n*{description}*",
                    val goal: String = "🎯 **{username} has reached the goal {title}**\n\n*{description}*",
                    val challenge: String = "🏅 **{username} has completed the challenge {title}**\n\n*{description}*",
                )

                data class LangTelegram(
                    val serverStarted: String = "✅ **Server started!**",
                    val serverStopped: String = "❌ **Server stopped!**",

                    val playerJoined: String = "🥳 **{username} joined the game**",
                    val playerLeft: String = "😕 **{username} left the game**",
                    val playerDied: String = "☠️ **{deathMessage}**",

                    val chatMessage: String = "**[{username}]** {text}",

                    val advancements: LangAdvancements = LangAdvancements(),

                    val playerList: String = "📝 **{count} players online:** {usernames}",
                    val playerListZeroOnline: String = "📝 **0 players online**",
                )

                data class MessageMeta(
                    val messageFormat: String = "§b<{sender}> §r{text}",
                    val hoverOpenInTelegram: String = "Open in Telegram",
                    val hoverOpenInBrowser: String = "Open in Web Browser",
                    val hoverCopyToClipboard: String = "Copy to clipboard",
                    val hoverTagToReply: String = "Tag him/her",
                    val reply: String = "[R {sender}: {text}]",
                    val replyToMinecraft: String = "[R {text}]",
                    val forward: String = "[F {from}]",
                    val gif: String = "[GIF]",
                    val document: String = "[Document]",
                    val photo: String = "[Photo]",
                    val audio: String = "[Audio]",
                    val sticker: String = "[Sticker]",
                    val video: String = "[Video]",
                    val videoMessage: String = "[Video message]",
                    val voiceMessage: String = "[Voice message]",
                    val poll: String = "[Poll: {title}]",
                    val pin: String = "[pinned a message]",
                )

                data class MessageFormatting(
                    val linkColor: String = "#FFFF55",
                    val linkFormatting: List<TextDecoration>? = Collections.singletonList(TextDecoration.UNDERLINED),
                    val mentionColor: String = "#FFFF55",
                    val mentionFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val hashtagColor: String = "#FFFF55",
                    val hashtagFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val codeColor: String = "#AAAAAA",
                    val codeFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val spoilerColor: String = "#AAAAAA",
                    val spoilerFormatting: List<TextDecoration>? = Collections.singletonList(TextDecoration.OBFUSCATED),
                    val spoilerReplaceWithChar: String? = "▌",
                    val replyColor: String = "#AAAAAA",
                    val replyFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val forwardColor: String = "#AAAAAA",
                    val forwardFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val mediaColor: String = "#FFFF55",
                    val mediaFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val pinnedMessageColor: String = "#FFFF55",
                    val pinnedMessageFormatting: List<TextDecoration>? = Collections.emptyList(),
                )

                data class LangMinecraft(
                    val messageMeta: MessageMeta = MessageMeta(),
                    val messageFormatting: MessageFormatting = MessageFormatting(),
                )
            }
        }
    }
}