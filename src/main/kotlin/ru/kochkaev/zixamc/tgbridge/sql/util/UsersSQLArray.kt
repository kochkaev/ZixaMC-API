package ru.kochkaev.zixamc.tgbridge.sql.util

import com.google.common.reflect.TypeToken
import ru.kochkaev.zixamc.tgbridge.config.GsonManager.gson
import ru.kochkaev.zixamc.tgbridge.sql.MySQL
import ru.kochkaev.zixamc.tgbridge.sql.SQLUser

class UsersSQLArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<LinkedUser>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { gson.fromJson(it, object: TypeToken<List<LinkedUser>>(){}.type) },
) {
    fun contains(userId: Long?) =
        userId?.let { contains(LinkedUser(it))} ?: false
    fun contains(entity: SQLUser?) =
        contains(entity?.userId)
    fun add(userId: Long?) {
        userId?.also {
            add(LinkedUser(it))
        }
    }
    fun add(entity: SQLUser?) {
        add(entity?.userId)
    }
    fun remove(userId: Long?) {
        userId?.also {
            remove(LinkedUser(it))
        }
    }
    fun remove(entity: SQLUser?) {
        remove(entity?.userId)
    }
}
