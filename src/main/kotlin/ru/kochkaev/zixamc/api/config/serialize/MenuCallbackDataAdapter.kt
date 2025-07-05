package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.api.telegram.Menu
import java.lang.reflect.Type

class MenuCallbackDataAdapter: JsonDeserializer<Menu.MenuCallbackData<*>>, JsonSerializer<Menu.MenuCallbackData<*>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Menu.MenuCallbackData<*> {
        val jsonObject = json.asJsonObject
        val operation = jsonObject.get("operation").asString
        val type = if (jsonObject.has("additionalType")) jsonObject.get("additionalType").asString else null
        val typeClass = type?.let { Menu.integrationTypes[it] }
        val data = typeClass?.let { context.deserialize<Any>(
            jsonObject.get("additional"),
            it,
        ) } ?: Menu.MenuCallbackData.DummyAdditional()
        return Menu.MenuCallbackData.of(operation, (typeClass?.let { typeClass } ?: Menu.MenuCallbackData.DummyAdditional::class.java) as Class<Any>, data)
    }
    override fun serialize(
        src: Menu.MenuCallbackData<*>,
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