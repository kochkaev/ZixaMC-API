package ru.kochkaev.zixamc.api.telegram.model

/** Describes the birthdate of a user. */
data class TgBirthdate(
    /** Day of the user's birth; 1-31 */
    val day: Int,
    /** Month of the user's birth; 1-12 */
    val month: Int,
    /** Year of the user's birth */
    val year: Int?,
)
