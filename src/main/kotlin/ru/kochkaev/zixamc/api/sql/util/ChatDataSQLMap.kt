package ru.kochkaev.zixamc.api.sql.util

import com.google.common.reflect.TypeToken
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.sql.MySQL
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataType
import ru.kochkaev.zixamc.api.sql.feature.data.FeatureData
import ru.kochkaev.zixamc.api.sql.feature.FeatureType

class ChatDataSQLMap(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLMap<ChatDataType<*>, Any>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { gson.fromJson(it, object: TypeToken<Map<ChatDataType<*>, *>>(){}.type) },
    keySerializer = { it.serializedName },
    valDeserializer = { key, it -> gson.fromJson(it, key.model) },
) {
    @Suppress("UNCHECKED_CAST")
    fun <R> getCasted(key: ChatDataType<R>): R? =
        get(key)?.let { it as R }
}