package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.api.telegram.model.TgChatMember
import ru.kochkaev.zixamc.api.telegram.model.TgChatMemberStatuses
import java.lang.reflect.Type

class TgChatMemberAdapter() : JsonDeserializer<TgChatMember>, JsonSerializer<TgChatMember> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TgChatMember {
        val jsonObject = json.asJsonObject
        val type = context.deserialize<TgChatMemberStatuses>(jsonObject.get("status"), TgChatMemberStatuses::class.java)
//        val data = getDeserialized(context, jsonObject, typeMap[type])
        val data = context.deserialize<TgChatMember>(
            json,
            type.model,
        )
        return data
    }
    override fun serialize(
        src: TgChatMember,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val jsonObject = context.serialize(src).asJsonObject
        jsonObject.add("status", context.serialize(src.status))
        return jsonObject
    }

}