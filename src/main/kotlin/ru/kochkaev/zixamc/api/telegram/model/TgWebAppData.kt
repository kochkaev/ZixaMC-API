package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** Describes data sent from a Web App to the bot. */
data class TgWebAppData(
    /** The data. Be aware that a bad client can send arbitrary data in this field. */
    val data: String,
    /** Text of the web_app keyboard button from which the Web App was opened. Be aware that a bad client can send arbitrary data in this field. */
    @SerializedName("button_text")
    val buttonText: String,
)
/** Describes a Web App. */
data class TgWebAppInfo(
    /** An HTTPS URL of a Web App to be opened with additional data as specified in Initializing Web Apps */
    val url: String,
)
