package ru.kochkaev.zixamc.api.sql.util

import com.google.common.reflect.TypeToken
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.sql.MySQL
import ru.kochkaev.zixamc.api.sql.SQLUser

class UsersSQLArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<SQLUser>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { gson.fromJson(it, object: TypeToken<List<SQLUser>>(){}.type) },
) {
    fun contains(userId: Long?) =
        userId?.let { contains(SQLUser.getWithoutCheck(it))} ?: false
    fun add(userId: Long?) {
        userId?.also {
            add(SQLUser.getWithoutCheck(it))
        }
    }
    fun remove(userId: Long?) {
        userId?.also {
            remove(SQLUser.getWithoutCheck(it))
        }
    }
}
