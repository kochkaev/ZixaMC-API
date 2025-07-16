package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a service message about a new forum topic created in the chat. */
data class TgForumTopicCreated(
    /** Name of the topic */
    val name: String,
    /** Color of the topic icon in RGB format */
    @SerializedName("icon_color")
    val iconColor: Int,
    /** Unique identifier of the custom emoji shown as the topic icon */
    @SerializedName("icon_custom_emoji_id")
    val iconCustomEmojiId: String?,
)
/** This object represents a service message about an edited forum topic. */
data class TgForumTopicEdited(
    /** New name of the topic, if it was edited */
    val name: String?,
    /** New identifier of the custom emoji shown as the topic icon, if it was edited; an empty string if the icon was removed */
    @SerializedName("icon_custom_emoji_id")
    val iconCustomEmojiId: String?,
)
/** This object represents a service message about a forum topic closed in the chat. Currently holds no information. */
class TgForumTopicClosed
/** This object represents a service message about a forum topic reopened in the chat. Currently holds no information. */
class TgForumTopicReopened
/** This object represents a service message about General forum topic hidden in the chat. Currently holds no information. */
class TgGeneralForumTopicHidden
/** This object represents a service message about General forum topic unhidden in the chat. Currently holds no information. */
class TgGeneralForumTopicUnhidden