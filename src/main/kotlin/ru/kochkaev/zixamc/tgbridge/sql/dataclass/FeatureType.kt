package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.gson.annotations.JsonAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.FeatureTypeAdapter
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup

@JsonAdapter(FeatureTypeAdapter::class)
data class FeatureType<R: FeatureData>(
    val model: Class<R>,
    val serializedName: String,
    val tgDisplayName: () -> String = { serializedName },
    val tgDescription: () -> String = { "" },
    val checkAvailable: suspend (SQLGroup) -> Boolean = { true },
    val getDefault: (Int?) -> R = { model.getDeclaredConstructor().newInstance(it) },
)
