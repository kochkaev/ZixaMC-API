package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.api.sql.feature.data.FeatureData
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataType
import java.lang.reflect.Type

class ChatDataMapDeserializer : JsonDeserializer<Map<ChatDataType<*>, *>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Map<ChatDataType<*>, *> {
        val result = mutableMapOf<ChatDataType<*>, Any>()
        val jsonObject = json.asJsonObject
        for ((keyStr, valueElement) in jsonObject.entrySet()) {
            val key: ChatDataType<*> = context.deserialize(JsonParser.parseString(keyStr), ChatDataType::class.java)
            val concreteType: Type = key.model
            val value: Any = context.deserialize(valueElement, concreteType)
            result[key] = value
        }
        return result
    }
}