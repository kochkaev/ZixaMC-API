package ru.kochkaev.zixamc.tgbridge.sql.util

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import ru.kochkaev.zixamc.tgbridge.config.GsonManager.gson
import ru.kochkaev.zixamc.tgbridge.sql.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.sql.MySQL
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback

class CallbacksSQLArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<LinkedCallback>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { gson.fromJson(it, object: TypeToken<List<LinkedCallback>>(){}.type) },
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
