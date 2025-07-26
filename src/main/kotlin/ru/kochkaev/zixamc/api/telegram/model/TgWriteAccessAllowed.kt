package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a service message about a user allowing a bot to write messages after adding it to the attachment menu, launching a Web App from a link, or accepting an explicit request from a Web App sent by the method requestWriteAccess. */
data class TgWriteAccessAllowed(
    /** True, if the access was granted after the user accepted an explicit request from a Web App sent by the method requestWriteAccess */
    @SerializedName("from_request")
    val fromRequest: Boolean? = null,
    /** Name of the Web App, if the access was granted when the Web App was launched from a link */
    @SerializedName("web_app_name")
    val webAppName: String? = null,
    /** True, if the access was granted when the bot was added to the attachment or side menu */
    @SerializedName("from_attachment_menu")
    val fromAttachmentMenu: String? = null,
)
