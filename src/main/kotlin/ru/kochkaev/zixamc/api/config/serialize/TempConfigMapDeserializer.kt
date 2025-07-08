package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.api.config.TempConfig
import ru.kochkaev.zixamc.api.config.TempConfigValueType
import java.lang.reflect.Type

class TempConfigMapDeserializer : JsonDeserializer<Map<TempConfigValueType<*>, *>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Map<TempConfigValueType<*>, *> {
        val result = mutableMapOf<TempConfigValueType<*>, Any>()
        val jsonObject = json.asJsonObject
        for ((keyStr, valueElement) in jsonObject.entrySet()) {
            if (!TempConfig.registeredValueTypes.keys.contains(keyStr)) continue
            val key: TempConfigValueType<*> = context.deserialize(JsonParser.parseString(keyStr), TempConfigValueType::class.java)
            val concreteType: Type = key.model
            val value = context.deserialize<Any>(valueElement, concreteType)
            result[key] = value
        }
        return result
    }
}