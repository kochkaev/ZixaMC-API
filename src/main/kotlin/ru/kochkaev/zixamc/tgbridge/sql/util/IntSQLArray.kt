package ru.kochkaev.zixamc.tgbridge.sql.util

import ru.kochkaev.zixamc.tgbridge.sql.MySQL

class IntSQLArray(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLArray<Int>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn
)
