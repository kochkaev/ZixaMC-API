package ru.kochkaev.zixamc.api.sql.util

import ru.kochkaev.zixamc.api.sql.MySQL
import java.sql.Types

class NullableStringSQLField(
    sql: MySQL,
    column: String,
    uniqueId: Long,
    uniqueColumn: String,
    val sqlType: Int = Types.VARCHAR
): AbstractSQLField<String?>(
    sql = sql,
    column = column,
    uniqueId = uniqueId,
    uniqueColumn = uniqueColumn,
    type = String::class.java,
    setter = { ps, it -> if (it!=null) ps.setString(1, it) else ps.setNull(1, sqlType) },
    getter = { rs -> if (!rs.wasNull()) rs.getString(1) else null },
)