package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a point on the map. */
data class TgLocation(
    /** Latitude as defined by the sender */
    val latitude: Float,
    /** Longitude as defined by the sender */
    val longitude: Float,
    /** The radius of uncertainty for the location, measured in meters; 0-1500 */
    @SerializedName("horizontal_accuracy")
    val horizontalAccuracy: Float?,
    /** Time relative to the message sending date, during which the location can be updated; in seconds. For active live locations only. */
    @SerializedName("live_period")
    val livePeriod: Int?,
    /** The direction in which user is moving, in degrees; 1-360. For active live locations only. */
    val heading: Int?,
    /** The maximum distance for proximity alerts about approaching another chat member, in meters. For sent live locations only. */
    @SerializedName("proximity_alert_radius")
    val proximityAlertRadius: Int?,
)
