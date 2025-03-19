package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.gson.GsonBuilder
import ru.kochkaev.zixamc.tgbridge.config.TextData
import ru.kochkaev.zixamc.tgbridge.config.serialize.TextDataAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.TopicTypeAdapter
import ru.kochkaev.zixamc.tgbridge.sql.MySQL

class SQLTopicsMap(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLMap<Topic<out TopicData>, TopicData>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    serializer = { GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .enableComplexMapKeySerialization()
        .registerTypeAdapter(TextData::class.java, TextDataAdapter())
        .registerTypeAdapter(Topic::class.java, TopicTypeAdapter())
        .create()
        .toJson(it)
    },
    deserializer = { GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .enableComplexMapKeySerialization()
        .registerTypeAdapter(TextData::class.java, TextDataAdapter())
        .registerTypeAdapter(Topic::class.java, TopicTypeAdapter())
        .create()
        .fromJson<Map<Topic<out  TopicData>, TopicData>>(it, Map::class.java)
    },
    keySerializer = { it.serializedName },
    valSerializer = { GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .enableComplexMapKeySerialization()
        .registerTypeAdapter(TextData::class.java, TextDataAdapter())
        .registerTypeAdapter(Topic::class.java, TopicTypeAdapter())
        .create()
        .toJson(it)
    },
    valDeserializer = { key, it ->
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(TextData::class.java, TextDataAdapter())
            .registerTypeAdapter(Topic::class.java, TopicTypeAdapter())
            .create()
            .fromJson<TopicData>(it, key.model)
    },
) {
    fun <R: TopicData> getCasted(key: Topic<R>): R? =
        get(key)?.let { key.model.cast(it) }
    data class TopicMap(
        val map: Map<Topic<out  TopicData>, TopicData> = mapOf()
    )
}