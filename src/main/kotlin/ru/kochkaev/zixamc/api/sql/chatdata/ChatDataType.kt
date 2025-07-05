package ru.kochkaev.zixamc.api.sql.chatdata

import com.google.gson.annotations.JsonAdapter
import io.leangen.geantyref.TypeToken
import ru.kochkaev.zixamc.api.config.GsonManager
import ru.kochkaev.zixamc.api.config.serialize.ChatDataTypeAdapter
import ru.kochkaev.zixamc.api.sql.SQLGroup
import java.lang.reflect.Type

@JsonAdapter(ChatDataTypeAdapter::class)
open class ChatDataType<R>(
    val model: Type,
    val serializedName: String,
) {
    fun with(data: R, mod: (R) -> R): R =
        GsonManager.gson.let { gson ->
            gson.fromJson<R>(gson.toJson(data), model)
        } .let(mod)
}
