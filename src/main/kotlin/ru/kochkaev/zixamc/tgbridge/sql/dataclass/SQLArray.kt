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
    deserializer = { gson.fromJson(it, ArrayData::class.java).array },
    serializer = { "{\"array\":[${it.joinToString(", ") { "\"$this\"" }}]}" },
    valSerializer = { it },
)
