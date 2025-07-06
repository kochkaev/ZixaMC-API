package ru.kochkaev.zixamc.api.sql.util

import com.google.common.reflect.TypeToken
import ru.kochkaev.zixamc.api.ZixaMC
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.sql.MySQL
import java.sql.SQLException

open class AbstractSQLMap<T, V>(
    val sql: MySQL,
    val column: String,
    val uniqueId: Long,
    val uniqueColumn: String,
    val serializer: (Map<T, V>) -> String = { gson.toJson(it) },
    val deserializer: (String) -> Map<T, V> = { gson.fromJson(it, object: TypeToken<Map<T, V>>(){}.type) },
    val keySerializer: (T) -> String = { gson.toJson(it) },
    val valSerializer: (V) -> String = { gson.toJson(it) },
    val valDeserializer: (T, String) -> V = { key, serialized -> gson.fromJson(serialized, object: TypeToken<V>(){}.type) },
) {
    open fun getAll() =
        try {
            MySQL.reConnect()
            val preparedStatement =
                MySQL.MySQLConnection!!.prepareStatement("SELECT $column FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
            preparedStatement.setLong(1, uniqueId)
            val query = preparedStatement.executeQuery()
            query.next()
            deserializer(query.getString(1))
        } catch (e: SQLException) {
            ZixaMC.logger.error("Get SQLMap \"$column\" in table \"${sql.tableName}\" error", e)
            null
        }
    open fun get(key: T) =
        try {
            MySQL.reConnect()
            val preparedStatement =
                MySQL.MySQLConnection!!.prepareStatement("SELECT JSON_EXTRACT($column, '$.${keySerializer(key)}') FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
            preparedStatement.setLong(1, uniqueId)
            val query = preparedStatement.executeQuery()
            query.next()
            val raw = query.getString(1)
            if (!query.wasNull())
                valDeserializer(key, raw)
            else null
        } catch (e: SQLException) {
            ZixaMC.logger.error("Get SQLMap \"$column\" value in table \"${sql.tableName}\" error", e)
            null
        }

    open fun setAll(map: Map<T, V>) {
        try {
            MySQL.reConnect()
            val preparedStatement =
                MySQL.Companion.MySQLConnection!!.prepareStatement("UPDATE ${sql.tableName} SET $column = ? WHERE $uniqueColumn = ?;")
            preparedStatement.setString(1, serializer(map))
            preparedStatement.setLong(2, uniqueId)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMC.logger.error("Set SQLMap \"$column\" in table \"${sql.tableName}\" error", e)
        }
    }

    open fun contains(key: T): Boolean =
        try {
            MySQL.Companion.reConnect()
            val preparedStatement =
                MySQL.Companion.MySQLConnection!!.prepareStatement("SELECT JSON_CONTAINS_PATH($column, 'one', '$.${keySerializer(key)}') FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
            preparedStatement.setLong(1, uniqueId)
            val query = preparedStatement.executeQuery()
            query.next()
            query.getBoolean(1)
        } catch (e: SQLException) {
            ZixaMC.logger.error("Check contains in SQLMap \"$column\" in table \"${sql.tableName}\" error", e)
            false
        }
    open fun set(key: T, value: V) = try {
        MySQL.Companion.reConnect()
        val preparedStatement =
            MySQL.Companion.MySQLConnection!!.prepareStatement("UPDATE ${sql.tableName} SET $column = JSON_SET($column, '$.${keySerializer(key)}', JSON_EXTRACT(?, '$')) WHERE $uniqueColumn = ?;")
        val serialized = valSerializer(value)
        preparedStatement.setString(1, serialized)
        preparedStatement.setLong(2, uniqueId)
        preparedStatement.executeUpdate()
        true
    } catch (e: SQLException) {
        ZixaMC.logger.error("Set value by key in SQLMap \"$column\" in table \"${sql.tableName}\" error", e)
        false
    }
    open fun remove(key: T) = try {
        if (!contains(key)) false
        else {
            MySQL.Companion.reConnect()
            val statement = "UPDATE ${sql.tableName} " +
                    "SET $column = JSON_REMOVE($column, '$.${keySerializer(key)}') " +
                    "WHERE $uniqueColumn = ?;"
            val preparedStatement =
                MySQL.Companion.MySQLConnection!!.prepareStatement(statement)
            preparedStatement.setLong(1, uniqueId)
            preparedStatement.executeUpdate()
            true
        }
    } catch (e: SQLException) {
        ZixaMC.logger.error("Remove value from SQLMap \"$column\" in table \"${sql.tableName}\" error", e)
        false
    }
}