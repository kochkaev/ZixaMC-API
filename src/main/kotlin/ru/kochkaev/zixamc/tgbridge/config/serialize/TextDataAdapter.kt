package ru.kochkaev.zixamc.tgbridge.config.serialize

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import ru.kochkaev.zixamc.tgbridge.config.TextData

class TextDataAdapter() : TypeAdapter<TextData>() {

    override fun write(out: JsonWriter, value: TextData?) {
        if (value == null) out.nullValue()
        else out.value(value.raw)
    }

    override fun read(reader: JsonReader) =
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else TextData(reader.nextString())

}