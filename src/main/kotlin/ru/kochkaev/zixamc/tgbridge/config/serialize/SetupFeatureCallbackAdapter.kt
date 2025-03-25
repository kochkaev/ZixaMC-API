package ru.kochkaev.zixamc.tgbridge.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCallback
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.SetupFeatureCallback
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureType
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.FeatureData
import java.lang.reflect.Type
import kotlin.reflect.jvm.internal.impl.resolve.calls.inference.CapturedType

class SetupFeatureCallbackAdapter: JsonDeserializer<SetupFeatureCallback<out FeatureData>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SetupFeatureCallback<out FeatureData> {
        val jsonObject = json.asJsonObject
        val type = context.deserialize<FeatureType<out FeatureData>>(
            jsonObject.get("feature"),
            FeatureType::class.java)
        val data = context.deserialize<FeatureData>(
            jsonObject.get("temp"),
            type.model
        )
        val field = jsonObject.get("field").asString
        val arg = jsonObject.get("arg").asString
        @Suppress("UNCHECKED_CAST")
        return SetupFeatureCallback(type as FeatureType<FeatureData>, data, field, arg)
    }
//    override fun serialize(
//        src: SetupFeatureCallback<out FeatureData>,
//        typeOfSrc: Type,
//        context: JsonSerializationContext
//    ): JsonElement {
//        val jsonObject = JsonObject()
//        jsonObject.add("feature", context.serialize(src.feature))
//        jsonObject.add("temp", context.serialize(src.temp))
//        return jsonObject
//    }

}