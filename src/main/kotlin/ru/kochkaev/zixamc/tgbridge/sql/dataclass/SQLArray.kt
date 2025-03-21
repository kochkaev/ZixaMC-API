package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import ru.kochkaev.zixamc.tgbridge.sql.MySQL

class SQLArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<String>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    deserializer = { gson.fromJson<List<String>>(it, List::class.java) },
    serializer = { gson.toJson(it) },
    valSerializer = { it },
)
