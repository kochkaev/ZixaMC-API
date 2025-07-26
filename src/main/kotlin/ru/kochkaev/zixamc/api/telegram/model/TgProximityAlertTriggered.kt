package ru.kochkaev.zixamc.api.telegram.model

/** This object represents the content of a service message, sent whenever a user in the chat triggers a proximity alert set by another user. */
data class TgProximityAlertTriggered(
    /** User that triggered the alert */
    val traveler: TgUser,
    /** User that set the alert */
    val watcher: TgUser,
    /** The distance between the users */
    val distance: Int,
)
