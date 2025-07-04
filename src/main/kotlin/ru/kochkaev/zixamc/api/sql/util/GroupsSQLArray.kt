package ru.kochkaev.zixamc.api.sql.util

import com.google.common.reflect.TypeToken
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.sql.MySQL
import ru.kochkaev.zixamc.api.sql.SQLGroup

class GroupsSQLArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<SQLGroup>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { gson.fromJson(it, object: TypeToken<List<SQLGroup>>(){}.type) },
) {
    fun contains(chatId: Long?) =
        chatId?.let { contains(SQLGroup.getWithoutCheck(it))} ?: false
    fun add(chatId: Long?) {
        chatId?.also {
            add(SQLGroup.getWithoutCheck(it))
        }
    }
}
