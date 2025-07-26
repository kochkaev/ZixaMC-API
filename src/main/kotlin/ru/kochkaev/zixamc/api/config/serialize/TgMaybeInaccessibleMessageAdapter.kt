package ru.kochkaev.zixamc.api.config.serialize

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import ru.kochkaev.zixamc.api.telegram.model.TgInaccessibleMessage
import ru.kochkaev.zixamc.api.telegram.model.TgMaybeInaccessibleMessage
import ru.kochkaev.zixamc.api.telegram.model.TgMessage
import java.lang.reflect.Type

class TgMaybeInaccessibleMessageAdapter : JsonDeserializer<TgMaybeInaccessibleMessage> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TgMaybeInaccessibleMessage {
        val jsonObject = json.asJsonObject
        val date = jsonObject.get("date").asInt
        val data = context.deserialize<TgMaybeInaccessibleMessage>(
            json,
            if (date == 0) TgInaccessibleMessage::class.java else TgMessage::class.java,
        )
        return data
    }
}
