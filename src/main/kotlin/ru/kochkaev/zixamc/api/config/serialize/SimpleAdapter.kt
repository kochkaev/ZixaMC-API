package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

open class SimpleAdapter<R>(
    val reader: (JsonReader) -> R?,
    val writer: (JsonWriter, R) -> Unit
): TypeAdapter<R>() {
    override fun write(out: JsonWriter, value: R?) {
        if (value == null) out.nullValue()
        else writer(out, value)
    }

    override fun read(reader: JsonReader) =
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else reader(reader)

}