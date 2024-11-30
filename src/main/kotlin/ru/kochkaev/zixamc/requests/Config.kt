package ru.kochkaev.zixamc.requests


/**
 * @author kochkaev
 */
data class Config (
    val mySQL: MySQLDataClass = MySQLDataClass(),
    val requestsBot: RequestsBotDataClass = RequestsBotDataClass()
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
        val poll: PollDataClass = PollDataClass(),
        val text: TextDataClass = TextDataClass(),
    ) {
        data class PollDataClass (
            val autoCreatePoll: Boolean = false,
            val pollQuestion: String = "",
            val pollAnswerTrue: String = "",
            val pollAnswerNull: String = "",
            val pollAnswerFalse: String = "",
        )
        data class TextDataClass (
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
}