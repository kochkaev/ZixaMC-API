package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import ru.kochkaev.zixamc.tgbridge.config.serialize.LinkedCallbackAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.LinkedGroupAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.LinkedUserAdapter
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.sql.MySQL
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup

class SQLCallbacksArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<LinkedCallback>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(LinkedCallback::class.java, LinkedCallbackAdapter())
        .create()
        .fromJson(it, object: TypeToken<List<LinkedCallback>>(){}.type) },
    serializer = { GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(LinkedCallback::class.java, LinkedCallbackAdapter())
        .create()
        .toJson(it) },
    valSerializer = { GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(LinkedCallback::class.java, LinkedCallbackAdapter())
        .create()
        .toJson(it) }
) {
    fun contains(callbackId: Long?) =
        callbackId?.let { contains(LinkedCallback(it))} ?: false
    fun contains(callback: SQLCallback<out CallbackData>?) =
        contains(callback?.callbackId)
    fun add(callbackId: Long?) {
        callbackId?.also {
            add(LinkedCallback(it))
        }
    }
    fun add(callback: SQLCallback<out CallbackData>?) {
        add(callback?.callbackId)
    }
    fun addAll(list: List<Long>?) {
        list?.forEach { add(LinkedCallback(it)) }
    }
    fun addAllSQL(list: List<SQLCallback<out CallbackData>>?) {
        list?.forEach { add(it) }
    }
}
