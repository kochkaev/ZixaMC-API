package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object contains information about the users whose identifiers were shared with the bot using a KeyboardButtonRequestUsers button. */
data class TgUsersShared(
    /** Identifier of the request */
    @SerializedName("request_id")
    val requestId: Int,
    /** Information about users shared with the bot. */
    val users: List<TgSharedUser>,
)
/** This object contains information about a user that was shared with the bot using a KeyboardButtonRequestUsers button. */
data class TgSharedUser(
    /** Identifier of the shared user. This number may have more than 32 significant bits and some programming languages may have difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so 64-bit integers or double-precision float types are safe for storing these identifiers. The bot may not have access to the user and could be unable to use this identifier, unless the user is already known to the bot by some other means. */
    @SerializedName("user_id")
    val userId: Long,
    /** First name of the user, if the name was requested by the bot */
    @SerializedName("first_name")
    val firstName: String?,
    /** Last name of the user, if the name was requested by the bot */
    @SerializedName("last_name")
    val lastName: String?,
    /** Username of the user, if the username was requested by the bot */
    val username: String?,
    /** Available sizes of the chat photo, if the photo was requested by the bot */
    val photo: List<TgPhotoSize>?,
)