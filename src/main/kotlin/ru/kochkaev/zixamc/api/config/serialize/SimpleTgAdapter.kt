package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

open class SimpleTgAdapter <T> (
    val typeField: String = "type",
    val typeModel: Class<out TgTypeEnum>,
) : JsonDeserializer<T>, JsonSerializer<T> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): T {
        val jsonObject = json.asJsonObject
        val type = context.deserialize<TgTypeEnum>(jsonObject.get(typeField), typeModel)
//        val data = getDeserialized(context, jsonObject, typeMap[type])
        val data = context.deserialize<T>(
            json,
            type.model,
        )
        return data
    }

    override fun serialize(
        src: T,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = context.serialize(src).asJsonObject
        jsonObject.add(typeField, context.serialize(src?.javaClass?.getDeclaredField(typeField)?.apply { trySetAccessible() }?.get(src)))
        return jsonObject
    }
}
