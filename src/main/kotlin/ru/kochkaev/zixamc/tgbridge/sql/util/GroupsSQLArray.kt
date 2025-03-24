package ru.kochkaev.zixamc.tgbridge.sql.util

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import ru.kochkaev.zixamc.tgbridge.config.GsonManager.gson
import ru.kochkaev.zixamc.tgbridge.sql.MySQL
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup

class GroupsSQLArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<LinkedGroup>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { gson.fromJson(it, object: TypeToken<List<LinkedGroup>>(){}.type) },
) {
    fun contains(chatId: Long?) =
        chatId?.let { contains(LinkedGroup(it))} ?: false
    fun contains(group: SQLGroup?) =
        contains(group?.chatId)
    fun add(chatId: Long?) {
        chatId?.also {
            add(LinkedGroup(it))
        }
    }
    fun add(group: SQLGroup?) {
        add(group?.chatId)
    }
}
