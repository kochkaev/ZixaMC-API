package ru.kochkaev.zixamc.api.sql

import com.mysql.cj.jdbc.exceptions.CommunicationsException
import ru.kochkaev.zixamc.api.ZixaMC
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.config.ConfigSQL
import java.sql.*

/**
 * @author NikitaCartes
 */
abstract class MySQL {

    // TODO: Add local database support

    companion object {
        @JvmStatic
        var MySQLConnection: Connection? = null
        @JvmStatic
        val config: ConfigSQL
            get() = ConfigManager.config.mySQL

        @Throws(Exception::class)
        fun connect() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver")
                val uri =
                    "jdbc:mysql://" + config.host + "/" + config.database + "?autoReconnect=true"
                MySQLConnection = DriverManager.getConnection(uri, config.user, config.password)
            } catch (e: ClassNotFoundException) {
                MySQLConnection = null
                throw Exception("Failed setting up mysql DB", e)
            } catch (e: SQLException) {
                MySQLConnection = null
                throw Exception("Failed setting up mysql DB", e)
            }
        }
        @JvmStatic
        fun reConnect() {
            try {
                if (MySQLConnection == null || !MySQLConnection!!.isValid(5)) {
                    ZixaMC.logger.debug("Reconnecting to MySQL")
                    if (MySQLConnection != null) {
                        MySQLConnection!!.close()
                    }
                    connect()
                }
            } catch (e: Exception) {
                ZixaMC.logger.error("Mysql reconnect failed", e)
            } catch (e: SQLException) {
                ZixaMC.logger.error("Mysql reconnect failed", e)
            }
        }

        fun close() {
            try {
                if (MySQLConnection != null) {
                    MySQLConnection!!.close()
                    MySQLConnection = null
                }
            } catch (e: CommunicationsException) {
                ZixaMC.logger.error("Can't connect to database while closing", e)
            } catch (e: SQLException) {
                ZixaMC.logger.error("Database connection not closed", e)
            }
        }

        val isClosed: Boolean
            get() = MySQLConnection == null
    }

    abstract val tableName: String
    abstract fun getModel():String

    @Throws(Exception::class)
    fun connectTable() {
        try {
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?;")
            preparedStatement.setString(1, tableName)
            if (!preparedStatement.executeQuery().next()) {
                MySQLConnection!!.createStatement().executeUpdate(
                    getModel()
                )
                afterCreateTable()
            }
            MySQLConnection!!.createStatement().execute("""
                    CREATE FUNCTION IF NOT EXISTS json_as_array_append(jsonDoc JSON, elem VARCHAR(16))
                    RETURNS JSON
                    BEGIN
                        RETURN JSON_ARRAY_APPEND(jsonDoc, '$.array', elem);
                    END;
                """)
            MySQLConnection!!.createStatement().execute("""
                    CREATE FUNCTION IF NOT EXISTS json_as_array_contains(jsonDoc JSON, elem VARCHAR(16))
                    RETURNS BOOLEAN
                    BEGIN 
                        RETURN JSON_CONTAINS(jsonDoc, JSON_QUOTE(elem), '$.array'); 
                    END;
                """)
        } catch (e: ClassNotFoundException) {
            MySQLConnection = null
            throw Exception("Failed setting up mysql DB table $tableName", e)
        } catch (e: SQLException) {
            MySQLConnection = null
            throw Exception("Failed setting up mysql DB table $tableName", e)
        }
    }
    open fun afterCreateTable() {}
}