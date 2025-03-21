package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import ru.kochkaev.zixamc.tgbridge.config.serialize.LinkedUserAdapter
import ru.kochkaev.zixamc.tgbridge.sql.MySQL
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity

class SQLUsersArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<LinkedUser>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { GsonBuilder()
//        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(LinkedUser::class.java, LinkedUserAdapter())
        .create()
        .fromJson(it, object:TypeToken<List<LinkedUser>>(){}.type) },
    serializer = { GsonBuilder()
//        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(LinkedUser::class.java, LinkedUserAdapter())
        .create()
        .toJson(it) },
    valSerializer = { GsonBuilder()
//        .setPrettyPrinting()
        .disableHtmlEscaping()
        .serializeNulls()
        .registerTypeAdapter(LinkedUser::class.java, LinkedUserAdapter())
        .create()
        .toJson(it) }
) {
    fun contains(userId: Long?) =
        userId?.let { contains(LinkedUser(it))} ?: false
    fun contains(entity: SQLEntity?) =
        contains(entity?.userId)
    fun add(userId: Long?) {
        userId?.also {
            add(LinkedUser(it))
        }
    }
    fun add(entity: SQLEntity?) {
        add(entity?.userId)
    }
}
