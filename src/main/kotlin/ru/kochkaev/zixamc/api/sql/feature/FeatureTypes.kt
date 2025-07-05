package ru.kochkaev.zixamc.api.sql.feature

import ru.kochkaev.zixamc.api.sql.feature.data.FeatureData
import ru.kochkaev.zixamc.api.sql.feature.type.PlayersGroupFeatureType

object FeatureTypes {
    val PLAYERS_GROUP = PlayersGroupFeatureType

    val entries = hashMapOf<String, FeatureType<out FeatureData>>(
        PLAYERS_GROUP.serializedName to PLAYERS_GROUP,
    )
    fun registerType(type: FeatureType<*>) {
        entries[type.serializedName] = type
    }
}
