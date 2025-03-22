package ru.kochkaev.zixamc.tgbridge.config.serialize

import com.google.gson.*
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgChatMember
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgChatMemberStatuses
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCallback
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