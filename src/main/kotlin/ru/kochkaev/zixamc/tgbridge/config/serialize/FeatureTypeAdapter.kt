package ru.kochkaev.zixamc.tgbridge.config.serialize

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.FeatureType
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.FeatureTypes

class FeatureTypeAdapter() : TypeAdapter<FeatureType<*>>() {

    override fun write(out: JsonWriter, value: FeatureType<*>?) {
        if (value == null) out.nullValue()
        else out.value(value.serializedName)
    }

    override fun read(reader: JsonReader) =
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else FeatureTypes.entries[reader.nextString()]

}