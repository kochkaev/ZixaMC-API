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
        val isEnabled: Boolean = true,
        val botToken: String = "",
        val botAPIURL: String = "https://api.telegram.org",
        val pollTimeout: Int = 60,
        val targetChatId: Long = 0,
        val targetTopicId: Int = 0,
        val serverIP: String = "",
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
            val buttons: RequestsBotTextButtonsDataClass = RequestsBotTextButtonsDataClass(),
            val inputFields: RequestsBotTextInputFieldsDataClass = RequestsBotTextInputFieldsDataClass(),
            val commands: RequestsBotTextCommandsDataClass = RequestsBotTextCommandsDataClass(),
            val events: RequestsBotTextEventsDataClass = RequestsBotTextEventsDataClass(),
            val messages: RequestsBotTextCreateRequestDataClass = RequestsBotTextCreateRequestDataClass(),
        ) {
            data class RequestsBotTextButtonsDataClass (
                val textButtonCreateRequest: String = "",
                val textButtonConfirmSending: String = "",
                val textButtonAgreeWithRules: String = "",
                val textButtonRedrawRequest: String = "",
                val textButtonCancelRequest: String = "",
                val textButtonJoinToPlayersGroup: String = "",
                val textButtonCopyServerIP: String = "",
            )
            data class RequestsBotTextInputFieldsDataClass (
                val textInputFieldPlaceholderNickname: String = "",
                val textInputFieldPlaceholderRequest: String = "",
            )
            data class RequestsBotTextCommandsDataClass (
                val textSyntaxAcceptHelp: String = "",
                val textSyntaxRejectHelp: String = "",
                val textSyntaxPromoteHelp: String = "",
                val textSyntaxKickHelp: String = "",
                val textSyntaxRestrictHelp: String = "",
                val textSyntaxLeavedHelp: String = "",
                val textSyntaxReturnHelp: String = "",
                val textCommandPermissionDenied: String = "",
            )
            data class RequestsBotTextEventsDataClass (
                val forTarget: RequestsBotTextEvents4TargetDataClass = RequestsBotTextEvents4TargetDataClass(),
                val forUser: RequestsBotTextEvents4UserDataClass = RequestsBotTextEvents4UserDataClass(),
            ) {
                data class RequestsBotTextEvents4TargetDataClass (
                    val textOnSend4Target: String = "",
                    val textOnAccept4Target: String = "",
                    val textOnReject4Target: String = "",
                    val textOnPromote4Target: String = "",
                    val textOnKick4Target: String = "",
                    val textOnRestrict4Target: String = "",
                    val textOnLeave4Target: String = "",
                    val textOnReturn4Target: String = "",
                    val textOnRulesUpdated4Target: String = "",
                    val textRequestCanceled4Target: String = "",
                )
                data class RequestsBotTextEvents4UserDataClass (
                    val textOnSend4User: String = "",
                    val textOnAccept4User: String = "",
                    val textOnReject4User: String = "",
                    val textOnKick4User: String = "",
                    val textOnRestrict4User: String = "",
                    val textOnLeave4User: String = "",
                    val textOnReturn4User: String = "",
                    val textOnRulesUpdated4User: String = "",
                    val textOnStart: String = "",
                    val textRequestCanceled4User: String = "",
                )
            }
            data class RequestsBotTextCreateRequestDataClass (
                val textNeedAgreeWithRules: String = "",
                val textMustAgreeWithRules: String = "",
                val textNeedNickname: String = "",
                val textWrongNickname: String = "",
                val textTakenNickname: String = "",
                val textConfirmSendRequest: String = "",
                val textYouAreNowCreatingRequest: String = "",
                val textYouHavePendingRequest: String = "",
                val textCancelRequest: String = "",
                val textYouAreNowPlayer: String = "",
                val textInfoMessage: String = "",
                val textOnNewRequest: String = "",
            )
        }
    }
    data class ServerBotDataClass (
        val isEnabled: Boolean = true,
        val botToken: String = "",
        val botAPIURL: String = "https://api.telegram.org",
        val pollTimeout: Int = 60,
        val targetChatId: Long = 0,
        val targetTopicId: Int = 0,
        val mentionAllReplaceWith: String = "+",
        val chatSync: ServerBotChatSyncDataClass = ServerBotChatSyncDataClass(),
        val easyAuth: ServerBotEasyAuth = ServerBotEasyAuth(),
    ) {
        data class ServerBotChatSyncDataClass (
            val isEnabled: Boolean = true,
            val chatId: Long = 0,
            val topicId: Int? = 0,
            val messages: ServerBotChatSyncMessageDataClass = ServerBotChatSyncMessageDataClass(),
            val events: ServerBotChatSyncGameEventsDataClass = ServerBotChatSyncGameEventsDataClass(),
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
            data class ServerBotChatSyncMessageDataClass (
                val bluemapUrl: String? = null,
                val requirePrefixInMinecraft: String? = "",
                val keepPrefix: Boolean = false,
                val mergeWindow: Int? = 0,
                val replyInDifferentLine: Boolean = false,
                val styledTelegramMessagesInMinecraft: Boolean = true,
                val parseMarkdownInMinecraftToTelegramMessages: Boolean = true,
            )
            data class ServerBotChatSyncGameEventsDataClass (
                val advancementMessages: ServerBotChatSyncAdvancementsDataClass = ServerBotChatSyncAdvancementsDataClass(),
                val enableDeathMessages: Boolean = true,
                val enableJoinMessages: Boolean = true,
                val enableLeaveMessages: Boolean = true,
                val leaveJoinMergeWindow: Int? = 0,
            ) {
                data class ServerBotChatSyncAdvancementsDataClass(
                    val enable: Boolean = true,
                    val enableTask: Boolean = true,
                    val enableGoal: Boolean = true,
                    val enableChallenge: Boolean = true,
                    val showDescription: Boolean = true,
                )
            }
        }
        data class ServerBotEasyAuth (
            val isEnabled: Boolean = true,
            val langMinecraft: ServerBotEasyAuthLangMinecraft = ServerBotEasyAuthLangMinecraft(),
            val langTelegram: ServerBotEasyAuthLangTelegram = ServerBotEasyAuthLangTelegram(),
        ) {
            data class ServerBotEasyAuthLangMinecraft (
                val onApprove: String = "",
                val onDeny: String = "",
                val youAreNotPlayer: String = "",
                val onJoinTip: String = "",
            )
            data class ServerBotEasyAuthLangTelegram (
                val onApprove: String = "",
                val onDeny: String = "",
                val onJoinTip: String = "",
                val buttonApprove: String = "",
                val buttonDeny: String = "",
            )
        }
    }
}