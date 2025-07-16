package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a venue. */
data class TgVenue(
    /** Venue location. Can't be a live location */
    val location: TgLocation,
    /** Name of the venue */
    val title: String,
    /** Address of the venue */
    val address: String,
    /** Foursquare identifier of the venue */
    @SerializedName("foursquare_id")
    val foursquareId: String?,
    /** Foursquare type of the venue. (For example, “arts_entertainment/default”, “arts_entertainment/aquarium” or “food/icecream”.) */
    @SerializedName("foursquare_type")
    val foursquareType: String?,
    /** Google Places identifier of the venue */
    @SerializedName("google_place_id")
    val googlePlaceId: String?,
    /** Google Places type of the venue. (See supported types: https://developers.google.com/places/web-service/supported_types) */
    @SerializedName("google_place_type")
    val googlePlaceType: String?,
)
