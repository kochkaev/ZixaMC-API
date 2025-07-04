package ru.kochkaev.zixamc.api.sql.util

import com.google.common.reflect.TypeToken
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.sql.MySQL
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.callback.CallbackData

class CallbacksSQLArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<SQLCallback<out CallbackData>>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { gson.fromJson(it, object: TypeToken<List<SQLCallback<out CallbackData>>>(){}.type) },
) {
    fun contains(callbackId: Long?) =
        callbackId?.let { id -> SQLCallback.getWithoutCheck(id)?.let { contains(it) }} ?: false
    fun add(callbackId: Long?) {
        callbackId?.also { id ->
            SQLCallback.getWithoutCheck(id)?.let { add(it) }
        }
    }
    fun addAll(list: List<Long>?) {
        list?.forEach { id -> SQLCallback.getWithoutCheck(id)?.let { add(it) } }
    }
    fun addAllSQL(list: List<SQLCallback<out CallbackData>>?) {
        list?.forEach { add(it) }
    }
}
