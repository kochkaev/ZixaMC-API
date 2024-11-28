package ru.kochkaev.zixamc.requests


/**
 * @author kochkaev
 */
data class Config (
    val botToken: String = "",
    val botAPIURL: String = "https://api.telegram.org",
    val targetChatId: Long = 0,
    val targetTopicId: Int = 0,
    val autoCreatePoll: Boolean = false,
    val pollQuestion: String = "",
    val pollAnswerTrue: String = "",
    val pollAnswerNull: String = "",
    val pollAnswerFalse: String = "",
    val forwardBack: Boolean = false,
    val textHello: String = "",
    val textAgreeWithRules: String = "",
    val textOnAgreeWithRules: String = "",
    val textOnSend4User: String = "",
    val textOnSend4Target: String = "",
    val textOnAccept4User: String = "",
    val textOnAccept4Target: String = "",
    val textOnReject4User: String = "",
    val textOnReject4Target: String = "",
    val textOnStart: String = "",
    val mySQLHost: String = "",
    val mySQLDatabase: String = "",
    val mySQLUser: String = "",
    val mySQLPassword: String = "",
    val mySQLTable: String = "",
)