package ru.kochkaev.zixamc.api.sql.util

import com.google.common.reflect.TypeToken
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.sql.MySQL
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataType

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