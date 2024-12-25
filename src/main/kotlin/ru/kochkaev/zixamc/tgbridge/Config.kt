package ru.kochkaev.zixamc.tgbridge

import java.util.*
import net.kyori.adventure.text.format.TextDecoration


/**
 * @author kochkaev
 */
data class Config (
    val general: GeneralConfig = GeneralConfig(),
    val mySQL: MySQLDataClass = MySQLDataClass(),
    val requestsBot: RequestsBotDataClass = RequestsBotDataClass(),
    val serverBot: ServerBotDataClass = ServerBotDataClass(),
) {
    data class GeneralConfig (
        val serverIP: String = "",
        val lang: GeneralConfigLang = GeneralConfigLang()
    ) {
        data class GeneralConfigLang (
            val infoMessage: String = "",
            val buttonCopyServerIP: String = "",
        )
    }
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
        val playersGroupInviteLink: String = "https:/t.me/",
        val addWhitelistCommand: String = "easywhitelist add {nickname}",
        val removeWhitelistCommand: String = "easywhitelist add {nickname}",
        val user: RequestsBotForUser = RequestsBotForUser(),
        val target: RequestsBotForTarget = RequestsBotForTarget(),
        val forModerator: RequestsBotForModerator = RequestsBotForModerator(),
        val commonLang: RequestsBotCommonLang = RequestsBotCommonLang(),
    ) {
        data class RequestsBotForUser (
            val lang: RequestsBotForUserLang = RequestsBotForUserLang(),
        ) {
            data class RequestsBotForUserLang (
                val button: RequestsBotForUserLangButtons = RequestsBotForUserLangButtons(),
                val inputField: RequestsBotForUserLangInputFields = RequestsBotForUserLangInputFields(),
                val event: RequestsBotForUserLangEvents = RequestsBotForUserLangEvents(),
                val creating: RequestsBotForUserLangCreating = RequestsBotForUserLangCreating(),
            ) {
                data class RequestsBotForUserLangButtons (
                    val createRequest: String = "",
                    val confirmSending: String = "",
                    val agreeWithRules: String = "",
                    val redrawRequest: String = "",
                    val cancelRequest: String = "",
                    val joinToPlayersGroup: String = "",
                )
                data class RequestsBotForUserLangInputFields (
                    val enterNickname: String = "",
                    val enterRequestText: String = "",
                )
                data class RequestsBotForUserLangEvents (
                    val onStart: String = "",
                    val onSend: String = "",
                    val onApprove: String = "",
                    val onDeny: String = "",
                    val onRestrict: String = "",
                    val onAccept: String = "",
                    val onReject: String = "",
                    val onCanceled: String = "",
                    val onKick: String = "",
                    val onLeave: String = "",
                    val onReturn: String = "",
                    val onRulesUpdated: String = "",
                )
                data class RequestsBotForUserLangCreating (
                    val needAgreeWithRules: String = "",
                    val mustAgreeWithRules: String = "",
                    val needNickname: String = "",
                    val wrongNickname: String = "",
                    val takenNickname: String = "",
                    val needRequestText: String = "",
                    val confirmSendRequest: String = "",
                    val youAreNowCreatingRequest: String = "",
                    val youHavePendingRequest: String = "",
                    val doYouWantToCancelRequest: String = "",
                    val youAreNowPlayer: String = "",
                )
            }
        }
        data class RequestsBotForTarget (
            val chatId: Long = 0,
            val topicId: Int = 0,
            val lang: RequestsBotForTargetLang = RequestsBotForTargetLang(),
        ) {
            data class RequestsBotForTargetLang (
                val event: RequestsBotForTargetLangEvents = RequestsBotForTargetLangEvents(),
                val poll: RequestsBotForTargetLangPoll = RequestsBotForTargetLangPoll(),
            ) {
                data class RequestsBotForTargetLangEvents (
                    val onSend: String = "",
                    val onCanceled: String = "",
                    val onAccept: String = "",
                    val onReject: String = "",
                    val onRulesUpdated: String = "",
                    val onPromote: String = "",
                    val onKick: String = "",
                    val onRestrict: String = "",
                    val onLeave: String = "",
                    val onReturn: String = "",
                )
                data class RequestsBotForTargetLangPoll (
                    val question: String = "",
                    val answerTrue: String = "",
                    val answerNull: String = "",
                    val answerFalse: String = "",
                )
            }
        }
        data class RequestsBotForModerator (
            val chatId: Long = 0,
            val topicId: Int = 0,
            val lang: RequestsBotForModeratorLang = RequestsBotForModeratorLang(),
        ) {
            data class RequestsBotForModeratorLang (
                val button: RequestsBotForModeratorLangButtons = RequestsBotForModeratorLangButtons(),
                val event: RequestsBotForModeratorLangEvents = RequestsBotForModeratorLangEvents(),
            ) {
                data class RequestsBotForModeratorLangButtons (
                    val approveSending: String = "",
                    val denySending: String = "",
                    val restrictSender: String = "",
                    val closeRequestVote: String = "",
                )
                data class RequestsBotForModeratorLangEvents (
                    val onNew: String = "",
                    val onApprove: String = "",
                    val onVoteClosed: String = "",
                    val onUserRestricted: String = "",
                )
            }
        }
        data class RequestsBotCommonLang (
            val command: RequestsBotTextCommandsDataClass = RequestsBotTextCommandsDataClass(),
        ) {
            data class RequestsBotTextCommandsDataClass (
                val acceptHelp: String = "",
                val rejectHelp: String = "",
                val promoteHelp: String = "",
                val kickHelp: String = "",
                val restrictHelp: String = "",
                val leaveHelp: String = "",
                val returnHelp: String = "",
                val permissionDenied: String = "",
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
                val noHaveChatWithBot: String = "",
                val botUsername: String = "",
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