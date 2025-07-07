package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.api.sql.feature.data.FeatureData
import ru.kochkaev.zixamc.api.sql.feature.FeatureType
import ru.kochkaev.zixamc.api.sql.feature.FeatureTypes
import java.lang.reflect.Type

class FeatureMapDeserializer : JsonDeserializer<Map<FeatureType<out FeatureData>, FeatureData>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Map<FeatureType<out FeatureData>, FeatureData> {
        val result = mutableMapOf<FeatureType<out FeatureData>, FeatureData>()
        val jsonObject = json.asJsonObject
        for ((keyStr, valueElement) in jsonObject.entrySet()) {
            if (!FeatureTypes.entries.keys.contains(keyStr)) continue
            val key: FeatureType<out FeatureData> = context.deserialize(JsonParser.parseString(keyStr), FeatureType::class.java)
            val concreteType: Type = key.model
            val value: FeatureData = context.deserialize(valueElement, concreteType)
            result[key] = value
        }
        return result
    }
}