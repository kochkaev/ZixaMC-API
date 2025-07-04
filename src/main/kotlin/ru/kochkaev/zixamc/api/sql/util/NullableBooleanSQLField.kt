package ru.kochkaev.zixamc.api.sql.util

import ru.kochkaev.zixamc.api.sql.MySQL
import java.sql.Types

class NullableBooleanSQLField(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLField<Boolean?>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    setter = { ps, it -> if (it!=null) ps.setBoolean(1, it) else ps.setNull(1, Types.INTEGER) },
    getter = { rs -> if (!rs.wasNull()) rs.getBoolean(1) else null },
)