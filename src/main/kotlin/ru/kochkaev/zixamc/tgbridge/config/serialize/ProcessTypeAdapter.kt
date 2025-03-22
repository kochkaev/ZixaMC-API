package ru.kochkaev.zixamc.tgbridge.config.serialize

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.ProcessType
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.ProcessTypes

class ProcessTypeAdapter() : TypeAdapter<ProcessType<*>>() {

    override fun write(out: JsonWriter, value: ProcessType<*>?) {
        if (value == null) out.nullValue()
        else out.value(value.serializedName)
    }

    override fun read(reader: JsonReader) =
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            null
        } else ProcessTypes.entries[reader.nextString()]

}