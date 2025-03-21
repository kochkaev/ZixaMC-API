package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import ru.kochkaev.zixamc.tgbridge.config.serialize.LinkedGroupAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.LinkedUserAdapter
import ru.kochkaev.zixamc.tgbridge.sql.MySQL
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup

class SQLGroupsArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<LinkedGroup>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { GsonBuilder()
//        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(LinkedGroup::class.java, LinkedGroupAdapter())
        .create()
        .fromJson(it, object: TypeToken<List<LinkedGroup>>(){}.type) },
    serializer = { GsonBuilder()
//        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(LinkedGroup::class.java, LinkedGroupAdapter())
        .create()
        .toJson(it) },
    valSerializer = { GsonBuilder()
//        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(LinkedGroup::class.java, LinkedGroupAdapter())
        .create()
        .toJson(it) }
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
