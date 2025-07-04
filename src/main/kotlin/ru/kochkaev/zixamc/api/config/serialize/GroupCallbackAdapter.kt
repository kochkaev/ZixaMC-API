package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.Operations
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.GroupCallback
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.FeatureGroupCallback
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup.SetupFeatureCallback
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.FeatureData
import java.lang.reflect.Type

class GroupCallbackAdapter: JsonDeserializer<GroupCallback> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): GroupCallback {
        val jsonObject = json.asJsonObject
        val operation = context.deserialize<Operations>(
            jsonObject.get("operation"),
            Operations::class.java)
        val name = jsonObject.get("name")?.let {
            if (it.isJsonNull) null
            else it.asString
        }
        if (operation == Operations.SETUP_FEATURE) {
            val data = context.deserialize<SetupFeatureCallback<out FeatureData>>(
                jsonObject.get("data"),
                SetupFeatureCallback::class.java,
            )
            @Suppress("UNCHECKED_CAST")
            return FeatureGroupCallback(data as SetupFeatureCallback<FeatureData>, name)
        }
        return GroupCallback(operation, name)
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