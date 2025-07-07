package ru.kochkaev.zixamc.api.sql.util

import ru.kochkaev.zixamc.api.sql.MySQL

class IntSQLField(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLField<Int>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    type = Int::class.java,
    setter = { ps, it -> ps.setInt(1, it) },
    getter = { rs -> rs.getInt(1) },
)