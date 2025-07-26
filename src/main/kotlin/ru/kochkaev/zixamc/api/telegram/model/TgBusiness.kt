package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** Contains information about the start page settings of a Telegram Business account. */
data class TgBusinessIntro(
    /** Title text of the business intro */
    val title: String?,
    /** Message text of the business intro */
    val message: String?,
    /** Sticker of the business intro */
    val sticker: TgSticker?,
)
/** Contains information about the location of a Telegram Business account. */
data class TgBusinessLocation(
    /** Address of the business */
    val address: String,
    /** Location of the business */
    val location: TgLocation?,
)
/** Describes an interval of time during which a business is open. */
data class TgBusinessOpeningHoursInterval(
    /** The minute's sequence number in a week, starting on Monday, marking the start of the time interval during which the business is open; 0 - 7 * 24 * 60 */
    @SerializedName("opening_minute")
    val openingMinute: Int,
    /** The minute's sequence number in a week, starting on Monday, marking the end of the time interval during which the business is open; 0 - 8 * 24 * 60 */
    @SerializedName("closing_minute")
    val closingMinute: Int,
)
/** Describes the opening hours of a business. */
data class TgBusinessOpeningHours(
    /** Unique name of the time zone for which the opening hours are defined */
    @SerializedName("time_zone_name")
    val timeZoneName: String,
    /** List of time intervals describing business opening hours */
    @SerializedName("opening_hours")
    val openingHours: List<TgBusinessOpeningHoursInterval>,
)