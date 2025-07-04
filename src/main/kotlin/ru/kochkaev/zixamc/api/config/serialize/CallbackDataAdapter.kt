package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.callback.CallbackData
import ru.kochkaev.zixamc.api.sql.callback.TgCallback
import java.lang.reflect.Type

class CallbackDataAdapter: JsonDeserializer<TgCallback<out CallbackData>>, JsonSerializer<TgCallback<out CallbackData>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TgCallback<out CallbackData> {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type").asString
//        val data = getDeserialized(context, jsonObject, typeMap[type])
        val data = context.deserialize<CallbackData>(
            jsonObject.get("data"),
            SQLCallback.registries[type],
        )
        return TgCallback(type, data)
    }
    override fun serialize(
        src: TgCallback<out CallbackData>,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("type", src.type)
        jsonObject.add("data", context.serialize(src.data))
        return jsonObject
    }

}