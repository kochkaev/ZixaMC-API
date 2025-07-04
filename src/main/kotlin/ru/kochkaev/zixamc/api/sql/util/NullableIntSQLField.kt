package ru.kochkaev.zixamc.api.sql.util

import ru.kochkaev.zixamc.api.sql.MySQL
import java.sql.Types

class NullableIntSQLField(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLField<Int?>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    setter = { ps, it -> if (it!=null) ps.setInt(1, it) else ps.setNull(1, Types.INTEGER) },
    getter = { rs -> if (!rs.wasNull()) rs.getInt(1) else null },
)