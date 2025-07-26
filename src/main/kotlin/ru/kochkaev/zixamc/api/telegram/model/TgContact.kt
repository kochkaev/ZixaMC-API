package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a phone contact. */
data class TgContact(
    /** Contact's phone number */
    @SerializedName("phone_number")
    val phoneNumber: String,
    /** Contact's first name */
    @SerializedName("first_name")
    val firstName: String,
    /** Contact's last name */
    @SerializedName("last_name")
    val lastName: String?,
    /** Contact's user identifier in Telegram. This number may have more than 32 significant bits and some programming languages may have difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so a 64-bit integer or double-precision float type are safe for storing this identifier. */
    @SerializedName("user_id")
    val userId: Long?,
    /** Additional data about the contact in the form of a vCard */
    val vcard: String?,
)
