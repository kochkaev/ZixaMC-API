package ru.kochkaev.zixamc.api.sql.util

import com.google.common.reflect.TypeToken
import ru.kochkaev.zixamc.api.ZixaMC
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.sql.MySQL
import java.sql.SQLException

open class AbstractSQLArray<T>(
    val sql: MySQL,
    val column: String,
    val uniqueId: Long,
    val uniqueColumn: String,
    val serializer: (List<T>) -> String = { gson.toJson(it) },
    val deserializer: (String) -> List<T> = { gson.fromJson(it, object: TypeToken<List<T>>(){}.type) },
    val valSerializer: (T) -> String = { gson.toJson(it) },
) {
    open fun get() =
        try {
            MySQL.Companion.reConnect()
            val preparedStatement =
                MySQL.Companion.MySQLConnection!!.prepareStatement("SELECT $column FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
            preparedStatement.setLong(1, uniqueId)
            val query = preparedStatement.executeQuery()
            query.next()
            deserializer(query.getString(1))
        } catch (e: SQLException) {
            ZixaMC.logger.error("Get SQLArray \"$column\" in table \"${sql.tableName}\" error", e)
            null
        }
    open fun set(array: List<T>) {
        try {
            MySQL.Companion.reConnect()
            val preparedStatement =
                MySQL.Companion.MySQLConnection!!.prepareStatement("UPDATE ${sql.tableName} SET $column = ? WHERE $uniqueColumn = ?;")
            preparedStatement.setString(1, serializer(array))
            preparedStatement.setLong(2, uniqueId)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMC.logger.error("Set SQLArray \"$column\" in table \"${sql.tableName}\" error", e)
        }
    }

    open fun contains(value: T): Boolean =
        try {
            MySQL.Companion.reConnect()
            val preparedStatement =
                MySQL.Companion.MySQLConnection!!.prepareStatement("SELECT JSON_CONTAINS($column, JSON_QUOTE(?), '$') FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
            preparedStatement.setString(1, valSerializer(value))
            preparedStatement.setLong(2, uniqueId)
            val query = preparedStatement.executeQuery()
            query.next()
            query.getBoolean(1)
        } catch (e: SQLException) {
            ZixaMC.logger.error("Check contains in SQLArray \"$column\" in table \"${sql.tableName}\" error", e)
            false
        }
    open fun add(value: T) = try {
        if (contains(value)) false
        else {
            MySQL.Companion.reConnect()
            val preparedStatement =
                MySQL.Companion.MySQLConnection!!.prepareStatement("UPDATE ${sql.tableName} SET $column = JSON_ARRAY_APPEND($column, '$', ?) WHERE $uniqueColumn = ?;")
            preparedStatement.setString(1, valSerializer(value))
            preparedStatement.setLong(2, uniqueId)
            preparedStatement.executeUpdate()
            true
        }
    } catch (e: SQLException) {
        ZixaMC.logger.error("Add value to SQLArray \"$column\" in table \"${sql.tableName}\" error", e)
        false
    }
    open fun remove(value: T) = try {
        if (!contains(value)) false
        else {
            MySQL.Companion.reConnect()
            val statement = "UPDATE ${sql.tableName} " +
                    "SET $column = JSON_REMOVE($column, JSON_UNQUOTE(JSON_SEARCH($column, 'one', ?, NULL, '$'))) " +
                    "WHERE $uniqueColumn = ?;"
            val preparedStatement =
                MySQL.Companion.MySQLConnection!!.prepareStatement(statement)
            preparedStatement.setString(1, valSerializer(value))
            preparedStatement.setLong(2, uniqueId)
            preparedStatement.executeUpdate()
            true
        }
    } catch (e: SQLException) {
        ZixaMC.logger.error("Remove value from SQLArray \"$column\" in table \"${sql.tableName}\" error", e)
        false
    }
}