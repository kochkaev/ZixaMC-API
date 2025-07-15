package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a service message about a change in auto-delete timer settings. */
data class TgMessageAutoDeleteTimerChanged(
    /** New auto-delete time for messages in the chat; in seconds */
    @SerializedName("message_auto_delete_time")
    val messageAutoDeleteTime: Int
)
