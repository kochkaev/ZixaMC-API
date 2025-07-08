package ru.kochkaev.zixamc.api.config

import com.google.gson.annotations.JsonAdapter
import ru.kochkaev.zixamc.api.config.serialize.TempConfigValueTypeAdapter
import java.lang.reflect.Type

@JsonAdapter(TempConfigValueTypeAdapter::class)
data class TempConfigValueType<T>(
    val serializedName: String,
    val model: Type
) {
    companion object {
        fun <T> of(serializedName: String, model: Class<T>) =
            TempConfigValueType<T>(serializedName, model)
    }
}