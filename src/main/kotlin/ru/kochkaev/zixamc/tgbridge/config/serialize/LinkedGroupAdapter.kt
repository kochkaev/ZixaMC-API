package ru.kochkaev.zixamc.tgbridge.config.serialize

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.LinkedEntity
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.LinkedGroup
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.LinkedUser

class LinkedGroupAdapter() : TypeAdapter<LinkedGroup>() {
    override fun write(out: JsonWriter, value: LinkedGroup?) {
        if (value == null) out.nullValue()
        else {
            val key: Long = value.key
            out.value(key)
        }
    }

    override fun read(reader: JsonReader) =
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else LinkedGroup(reader.nextString().toLong())

}