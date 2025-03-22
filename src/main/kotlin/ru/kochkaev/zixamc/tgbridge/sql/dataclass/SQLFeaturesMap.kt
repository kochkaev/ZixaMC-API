package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import ru.kochkaev.zixamc.tgbridge.config.TextData
import ru.kochkaev.zixamc.tgbridge.config.serialize.TextDataAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.FeatureTypeAdapter
import ru.kochkaev.zixamc.tgbridge.sql.MySQL

class SQLFeaturesMap(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLMap<FeatureType<out FeatureData>, FeatureData>(
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
        .registerTypeAdapter(FeatureType::class.java, FeatureTypeAdapter())
        .create()
        .toJson(it)
    },
    deserializer = { GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .enableComplexMapKeySerialization()
        .registerTypeAdapter(TextData::class.java, TextDataAdapter())
        .registerTypeAdapter(FeatureType::class.java, FeatureTypeAdapter())
        .create()
        .fromJson(it, object: TypeToken<Map<FeatureType<out  FeatureData>, FeatureData>>(){}.type)
    },
    keySerializer = { it.serializedName },
    valSerializer = { GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .enableComplexMapKeySerialization()
        .registerTypeAdapter(TextData::class.java, TextDataAdapter())
        .registerTypeAdapter(FeatureType::class.java, FeatureTypeAdapter())
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
            .registerTypeAdapter(FeatureType::class.java, FeatureTypeAdapter())
            .create()
            .fromJson<FeatureData>(it, key.model)
    },
) {
    fun <R: FeatureData> getCasted(key: FeatureType<R>): R? =
        get(key)?.let { key.model.cast(it) }
}