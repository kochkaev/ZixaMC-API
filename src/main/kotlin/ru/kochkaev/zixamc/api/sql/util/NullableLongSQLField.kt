package ru.kochkaev.zixamc.api.sql.util

import ru.kochkaev.zixamc.api.sql.MySQL
import java.sql.Types

class NullableLongSQLField(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
): AbstractSQLField<Long?>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    setter = { ps, it -> if (it!=null) ps.setLong(1, it) else ps.setNull(1, Types.BIGINT) },
    getter = { rs -> if (!rs.wasNull()) rs.getLong(1) else null },
)