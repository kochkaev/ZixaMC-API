package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a service message about a video chat scheduled in the chat. */
data class TgVideoChatScheduled(
    /** Point in time (Unix timestamp) when the video chat is supposed to be started by a chat administrator */
    @SerializedName("start_date")
    val startDate: Int,
)
/** This object represents a service message about a video chat started in the chat. Currently holds no information. */
class TgVideoChatStarted
/** This object represents a service message about a video chat ended in the chat. */
data class TgVideoChatEnded(
    /** Video chat duration in seconds */
    val duration: Int,
)
/** This object represents a service message about new members invited to a video chat. */
data class TgVideoChatParticipantsInvited(
    /** New members that were invited to the video chat */
    val users: List<TgUser>,
)