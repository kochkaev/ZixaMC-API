package ru.kochkaev.zixamc.tgbridge.config.serialize

import com.google.gson.*
import java.lang.reflect.Type

class TgChatMemberAdapter() : JsonDeserializer<ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMember>, JsonSerializer<ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMember> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMember {
        val jsonObject = json.asJsonObject
        val type = context.deserialize<ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMemberStatuses>(jsonObject.get("status"), ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMemberStatuses::class.java)
//        val data = getDeserialized(context, jsonObject, typeMap[type])
        val data = context.deserialize<ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMember>(
            json,
            type.model,
        )
        return data
    }
    override fun serialize(
        src: ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMember,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = context.serialize(src).asJsonObject
        jsonObject.add("status", context.serialize(src.status))
        return jsonObject
    }

}