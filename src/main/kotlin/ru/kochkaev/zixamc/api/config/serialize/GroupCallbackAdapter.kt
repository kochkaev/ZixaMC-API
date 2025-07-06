package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup.GroupCallback
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup
import java.lang.reflect.Type

class GroupCallbackAdapter: JsonDeserializer<GroupCallback<*>>, JsonSerializer<GroupCallback<*>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): GroupCallback<*> {
        val jsonObject = json.asJsonObject
        val operation = jsonObject.get("operation").asString
        val type = if (jsonObject.has("additionalType")) jsonObject.get("additionalType").asString else null
        val typeClass = type?.let { ServerBotGroup.integrationTypes[it] }
        val data = typeClass?.let { context.deserialize<Any>(
            jsonObject.get("additional"),
            it,
        ) } ?: GroupCallback.DummyAdditional()
        return GroupCallback.of(operation, (typeClass ?: GroupCallback.DummyAdditional::class.java) as Class<Any>, data)
    }
    override fun serialize(
        src: GroupCallback<*>,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = JsonObject()
        jsonObject.addProperty("operation", src.operation)
        jsonObject.addProperty("additionalType", src.additionalType)
        jsonObject.add("additional", context.serialize(src.additional))
        return jsonObject
    }
}