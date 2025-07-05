package ru.kochkaev.zixamc.api.sql.util

import com.google.common.reflect.TypeToken
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.sql.MySQL
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.feature.data.FeatureData
import ru.kochkaev.zixamc.api.sql.feature.FeatureType

class FeaturesSQLMap(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
    group: SQLGroup,
): AbstractSQLMap<FeatureType<out FeatureData>, FeatureData>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { gson.fromJson(it, object: TypeToken<Map<FeatureType<out FeatureData>, FeatureData>>(){}.type) },
    keySerializer = { it.serializedName },
    valDeserializer = { key, it -> gson.fromJson<FeatureData>(it, key.model).apply { this.group = group } },
) {
    fun <R: FeatureData> getCasted(key: FeatureType<R>): R? =
        get(key)?.let { key.model.cast(it) }
}