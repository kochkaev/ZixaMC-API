package ru.kochkaev.zixamc.tgbridge.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.FeatureData
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.FeatureType
import java.lang.reflect.Type

class FeatureMapDeserializer : JsonDeserializer<Map<FeatureType<out FeatureData>, FeatureData>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Map<FeatureType<out FeatureData>, FeatureData> {
        val result = mutableMapOf<FeatureType<out FeatureData>, FeatureData>()

        // При условии, что ключи сериализуются как объекты JSON
        val jsonObject = json.asJsonObject
        for ((keyStr, valueElement) in jsonObject.entrySet()) {
            // Десериализуем ключ. Если ключи сложные, возможно, придётся использовать другой подход.
            val key: FeatureType<out FeatureData> = context.deserialize(JsonParser.parseString(keyStr), FeatureType::class.java)

            // Используем поле model ключа для десериализации значения
            val concreteType: Type = key.model
            val value: FeatureData = context.deserialize(valueElement, concreteType)
            result[key] = value
        }
        return result
    }
}