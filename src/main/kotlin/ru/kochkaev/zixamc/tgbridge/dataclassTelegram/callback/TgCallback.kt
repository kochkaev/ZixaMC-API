package ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgCallbackQuery
import ru.kochkaev.zixamc.tgbridge.config.serialize.CallbackDataAdapter

data class TgCallback<T: CallbackData> (
    @SerializedName("t")
    val type: String,
    @SerializedName("d")
    val data: T? = null,
) {
    fun serialize(): String =
        try {
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
                .toJson(this)
        } catch (_: Exception) {
            ""
        }
    companion object {
        fun deserialize(query: TgCallbackQuery, typeMap: HashMap<String, Class<out CallbackData>>): TgCallback<out CallbackData>? {
            return try {
                GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .registerTypeAdapter(TgCallback::class.java, CallbackDataAdapter(typeMap))
                    .create()
                    .fromJson<TgCallback<CallbackData>>(
                        query.data,
                        TgCallback::class.java
                    )
            } catch (_: Exception) {
                null
            }
        }
    }
}