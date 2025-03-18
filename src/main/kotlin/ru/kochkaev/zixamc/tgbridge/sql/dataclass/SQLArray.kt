package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.gson.Gson
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.sql.MySQL
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.MySQLConnection
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.reConnect
import java.sql.SQLException

class SQLArray(
    val sql: MySQL,
    private val column: String,
    private val uniqueId: Long,
    private val uniqueColumn: String,
) {
    companion object {
        val gson = Gson()
    }
    fun get() =
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT $column FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
            preparedStatement.setLong(1, uniqueId)
            val query = preparedStatement.executeQuery()
            query.next()
            gson.fromJson(query.getString(1), ArrayData::class.java).array
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
            null
        }
    fun set(array: Array<String>) {
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE ${sql.tableName} SET $column = ? WHERE $uniqueColumn = ?;")
            preparedStatement.setString(1, "{\"array\":[${array.joinToString(", ") { "\"$this\"" }}]}")
            preparedStatement.setLong(2, uniqueId)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }

    fun contains(value: String): Boolean =
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT JSON_CONTAINS($column, JSON_QUOTE(?), '$.array') FROM ${sql.tableName} WHERE $uniqueColumn = ?;")
            preparedStatement.setString(1, value)
            preparedStatement.setLong(2, uniqueId)
            val query = preparedStatement.executeQuery()
            query.next()
            query.getBoolean(1)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isRegistered error", e)
            false
        }
    fun add(value: String) = try {
        reConnect()
        val preparedStatement =
            MySQLConnection!!.prepareStatement("UPDATE ${sql.tableName} SET $column = JSON_ARRAY_APPEND($column, '$.array', ?) WHERE $uniqueColumn = ?;")
        preparedStatement.setString(1, value)
        preparedStatement.setLong(2, uniqueId)
        preparedStatement.executeUpdate()
        true
    } catch (e: SQLException) {
        ZixaMCTGBridge.logger.error("updateUserData error", e)
        false
    }
    fun remove(value: String) = try {
        if (!contains(value)) false
        else {
            reConnect()
            val statement = "UPDATE ${sql.tableName} " +
                    "SET $column = JSON_REMOVE($column, JSON_UNQUOTE(JSON_SEARCH($column, 'one', ?, NULL, '$.array'))) " +
                    "WHERE $uniqueColumn = ?;"
            ZixaMCTGBridge.logger.info(statement)
            val preparedStatement =
                MySQLConnection!!.prepareStatement(statement)
            preparedStatement.setString(1, value)
            preparedStatement.setLong(2, uniqueId)
            preparedStatement.executeUpdate()
            true
        }
    } catch (e: SQLException) {
        ZixaMCTGBridge.logger.error("updateUserData error", e)
        false
    }
}