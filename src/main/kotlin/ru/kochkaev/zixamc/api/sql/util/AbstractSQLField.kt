package ru.kochkaev.zixamc.api.sql.util

import com.google.common.reflect.TypeToken
import ru.kochkaev.zixamc.api.ZixaMC
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.sql.MySQL
import java.lang.reflect.Type
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

open class AbstractSQLField<T>(
    val sql: MySQL,
    val column: String,
    val uniqueId: Long,
    val uniqueColumn: String,
    val type: Type? = object: TypeToken<T>(){}.type,
    val setter: (PreparedStatement, T) -> Unit = { ps, it -> ps.setString(1, gson.toJson(it)) },
    val getter: (ResultSet) -> T = { rs -> gson.fromJson(rs.getString(1), type) },
) {
    protected open fun exists(): Boolean = try {
        MySQL.reConnect()
        val preparedStatement =
            MySQL.MySQLConnection!!.prepareStatement("SELECT * FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
        preparedStatement.setLong(1, uniqueId)
        val query = preparedStatement.executeQuery()
        query.next()
    } catch (e: SQLException) {
        ZixaMC.logger.error("Is exists \"$uniqueColumn = $uniqueId\" in table \"${sql.tableName}\" error due operation with \"$column\" column", e)
        false
    }
    open fun get(): T? = try {
        MySQL.reConnect()
        val preparedStatement =
            MySQL.MySQLConnection!!.prepareStatement("SELECT $column FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
        preparedStatement.setLong(1, uniqueId)
        val query = preparedStatement.executeQuery()
        if (query.next())
            getter(query)
        else null
    } catch (e: SQLException) {
        ZixaMC.logger.error("Get column \"$column\" in table \"${sql.tableName}\" error", e)
        null
    }
    open fun set(value: T): Boolean = try {
        if (!exists()) false
        else {
            MySQL.reConnect()
            val preparedStatement =
                MySQL.MySQLConnection!!.prepareStatement("UPDATE ${sql.tableName} SET $column = ? WHERE $uniqueColumn = ?;")
            setter(preparedStatement, value)
            preparedStatement.setLong(2, uniqueId)
            preparedStatement.executeUpdate()
            true
        }
    } catch (e: SQLException) {
        ZixaMC.logger.error("Set column \"$column\" in table \"${sql.tableName}\" error", e)
        false
    }
}