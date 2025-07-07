package ru.kochkaev.zixamc.api.sql.util

import ru.kochkaev.zixamc.api.sql.MySQL

class StringSQLField(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLField<String>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    type = String::class.java,
    setter = { ps, it -> ps.setString(1, it) },
    getter = { rs -> rs.getString(1) },
)