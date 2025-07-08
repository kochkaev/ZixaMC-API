package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.api.telegram.AdminPanel
import java.lang.reflect.Type

class AdminPanelCallbackAdapter: JsonDeserializer<AdminPanel.AdminPanelCallback<*>>, JsonSerializer<AdminPanel.AdminPanelCallback<*>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AdminPanel.AdminPanelCallback<*> {
        val jsonObject = json.asJsonObject
        val operation = jsonObject.get("operation").asString
        val type = if (jsonObject.has("additionalType")) jsonObject.get("additionalType").asString else null
        val typeClass = type?.let { AdminPanel.integrationTypes[it] }
        val data = typeClass?.let { context.deserialize<Any>(
            jsonObject.get("additional"),
            it,
        ) } ?: AdminPanel.AdminPanelCallback.DummyAdditional()
        return AdminPanel.AdminPanelCallback.of(operation, (typeClass ?: AdminPanel.AdminPanelCallback.DummyAdditional::class.java) as Class<Any>, data)
    }
    override fun serialize(
        src: AdminPanel.AdminPanelCallback<*>,
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