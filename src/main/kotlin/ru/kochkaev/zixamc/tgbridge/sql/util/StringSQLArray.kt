package ru.kochkaev.zixamc.tgbridge.sql.util

import ru.kochkaev.zixamc.tgbridge.sql.MySQL

class StringSQLArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<String>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    valSerializer = { it },
)
