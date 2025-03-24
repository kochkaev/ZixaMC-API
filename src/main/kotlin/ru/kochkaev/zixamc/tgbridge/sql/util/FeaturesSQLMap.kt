package ru.kochkaev.zixamc.tgbridge.sql.util

import com.google.common.reflect.TypeToken
import ru.kochkaev.zixamc.tgbridge.config.GsonManager.gson
import ru.kochkaev.zixamc.tgbridge.sql.MySQL
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.FeatureData
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureType

class FeaturesSQLMap(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLMap<FeatureType<out FeatureData>, FeatureData>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { gson.fromJson(it, object: TypeToken<Map<FeatureType<out FeatureData>, FeatureData>>(){}.type) },
    keySerializer = { it.serializedName },
    valDeserializer = { key, it -> gson.fromJson<FeatureData>(it, key.model) },
) {
    fun <R: FeatureData> getCasted(key: FeatureType<R>): R? =
        get(key)?.let { key.model.cast(it) }
}