package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.gson.Gson
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.sql.MySQL
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.MySQLConnection
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.reConnect
import java.sql.SQLException

open class AbstractSQLMap<T, V>(
    val sql: MySQL,
    private val column: String,
    private val uniqueId: Long,
    private val uniqueColumn: String,
    private val serializer: (Map<T, V>) -> String,
    private val deserializer: (String) -> Map<T, V>,
    private val keySerializer: (T) -> String,
    private val valSerializer: (V) -> String,
    private val valDeserializer: (T, String) -> V,
) {
    companion object {
        val gson = Gson()
    }
    fun getAll() =
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT $column FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
            preparedStatement.setLong(1, uniqueId)
            val query = preparedStatement.executeQuery()
            query.next()
            deserializer(query.getString(1))
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
            null
        }
    fun get(key: T) =
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT JSON_EXTRACT($column, '$.${keySerializer(key)}') FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
            preparedStatement.setLong(1, uniqueId)
            val query = preparedStatement.executeQuery()
            query.next()
            val raw = query.getString(1)
            if (!query.wasNull())
                valDeserializer(key, raw)
            else null
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
            null
        }

    fun setAll(map: Map<T, V>) {
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE ${sql.tableName} SET $column = ? WHERE $uniqueColumn = ?;")
            preparedStatement.setString(1, serializer(map))
            preparedStatement.setLong(2, uniqueId)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }

    fun contains(key: T): Boolean =
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT JSON_CONTAINS_PATH($column, 'one', '$.${keySerializer(key)}') FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
            preparedStatement.setLong(1, uniqueId)
            val query = preparedStatement.executeQuery()
            query.next()
            query.getBoolean(1)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isRegistered error", e)
            false
        }
    fun set(key: T, value: V) = try {
        reConnect()
        val preparedStatement =
            MySQLConnection!!.prepareStatement("UPDATE ${sql.tableName} SET $column = JSON_SET($column, '$.${keySerializer(key)}', ?) WHERE $uniqueColumn = ?;")
        preparedStatement.setString(1, valSerializer(value))
        preparedStatement.setLong(2, uniqueId)
        preparedStatement.executeUpdate()
        true
    } catch (e: SQLException) {
        ZixaMCTGBridge.logger.error("updateUserData error", e)
        false
    }
    fun remove(key: T) = try {
        if (!contains(key)) false
        else {
            reConnect()
            val statement = "UPDATE ${sql.tableName} " +
                    "SET $column = JSON_REMOVE($column, '$.${keySerializer(key)}') " +
                    "WHERE $uniqueColumn = ?;"
            ZixaMCTGBridge.logger.info(statement)
            val preparedStatement =
                MySQLConnection!!.prepareStatement(statement)
            preparedStatement.setLong(1, uniqueId)
            preparedStatement.executeUpdate()
            true
        }
    } catch (e: SQLException) {
        ZixaMCTGBridge.logger.error("updateUserData error", e)
        false
    }
}