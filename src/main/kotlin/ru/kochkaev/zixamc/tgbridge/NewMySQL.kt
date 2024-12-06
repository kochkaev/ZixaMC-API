package ru.kochkaev.zixamc.tgbridge

import com.google.gson.Gson
import com.mysql.cj.jdbc.exceptions.CommunicationsException
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.*
import ru.kochkaev.zixamc.tgbridge.legecySQL.AccountData
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.ArrayData
import java.sql.*

/**
 * @author NikitaCartes
 */
class NewMySQL {
    private var MySQLConnection: Connection? = null
    private lateinit var config: Config.MySQLDataClass
    val gson = Gson()

    /* Account types:
        0 -> Admin
        1 -> Player
        2 -> Requester
        3 -> Unknown
     */
    @Throws(Exception::class)
    fun connect() {
        config = ConfigManager.CONFIG!!.mySQL
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val uri =
                "jdbc:mysql://" + config.mySQLHost + "/" + config.mySQLDatabase + "?autoReconnect=true"
            MySQLConnection = DriverManager.getConnection(uri, config.mySQLUser, config.mySQLPassword)
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?;")
            preparedStatement.setString(1, config.mySQLTable)
            if (!preparedStatement.executeQuery().next()) {
                MySQLConnection!!.createStatement().executeUpdate(
                    java.lang.String.format(
                        """
                                        CREATE TABLE `%s`.`%s` (
                                            `id` INT NOT NULL AUTO_INCREMENT,
                                            `user_id` BIGINT NOT NULL,
                                            `nickname` VARCHAR(16),
                                            `nicknames` JSON,
                                            `account_type` INT NOT NULL,
                                            `temp_array` JSON NOT NULL,
                                            `data` JSON NOT NULL,
                                            PRIMARY KEY (`id`), UNIQUE (`user_id`)
                                        ) ENGINE = InnoDB;
                                        """.trimIndent(),
                        config.mySQLDatabase,
                        config.mySQLTable
                    )
                )
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
            throw Exception("Failed setting up mysql DB", e)
        } catch (e: SQLException) {
            MySQLConnection = null
            throw Exception("Failed setting up mysql DB", e)
        }
    }

    private fun reConnect() {
        try {
            if (MySQLConnection == null || !MySQLConnection!!.isValid(5)) {
                ZixaMCTGBridge.logger.debug("Reconnecting to MySQL")
                if (MySQLConnection != null) {
                    MySQLConnection!!.close()
                }
                connect()
            }
        } catch (e: Exception) {
            ZixaMCTGBridge.logger.error("Mysql reconnect failed", e)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Mysql reconnect failed", e)
        }
    }

    fun close() {
        try {
            if (MySQLConnection != null) {
                MySQLConnection!!.close()
                MySQLConnection = null
            }
        } catch (e: CommunicationsException) {
            ZixaMCTGBridge.logger.error("Can't connect to database while closing", e)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Database connection not closed", e)
        }
    }

    val isClosed: Boolean
        get() = MySQLConnection == null


    fun registerUser(userId: Long?, nickname: String?, nicknames: Array<String>?, accountType: Int?, data: AccountData?): Boolean =
        registerUser(userId, nickname, nicknames, accountType, Gson().toJson(data))
    fun registerUser(userId: Long?, nickname: String?, nicknames: Array<String>?, accountType: Int?, data: String?): Boolean {
        try {
            reConnect()
            if (!isUserRegistered(userId) && userId != null && !isNicknameRegistered(nickname) && (nicknames?.any{isNicknameRegistered(it)} != true)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("INSERT INTO " + config.mySQLTable + " (user_id, nickname, nicknames, account_type, temp_array, data) VALUES (?, ?, ?, ?, ?, ?);")
                preparedStatement.setLong(1, userId)
                preparedStatement.setString(2, nickname)
                preparedStatement.setString(3, "{\"array\":[${nicknames?.joinToString(", ") { "\"$this\"" }}]}")
                preparedStatement.setInt(4, accountType?:3)
                preparedStatement.setString(5, "{\"array\":[]}")
                preparedStatement.setString(6, data)
                preparedStatement.executeUpdate()
                return true
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Register error ", e)
        }
        return false
    }

    fun isUserRegistered(userId: Long?): Boolean {
        try {
            if (userId == null) return false
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM " + config.mySQLTable + " WHERE user_id = ?;")
            preparedStatement.setLong(1, userId)
            return preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isUserRegistered error", e)
        }
        return false
    }
    fun isNicknameRegistered(nickname: String?): Boolean {
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM " + config.mySQLTable + " WHERE nickname = ? OR json_as_array_contains(nicknames, ?);")
            preparedStatement.setString(1, nickname)
            preparedStatement.setString(2, nickname)
            return preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isUserRegistered error", e)
        }
        return false
    }
    fun isNicknameNotAvailableToRegister(userId: Long?, nickname: String?): Boolean {
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM " + config.mySQLTable + " WHERE user_id = ? AND (nickname = ? OR json_as_array_contains(nicknames, ?));")
            preparedStatement.setString(1, nickname)
            preparedStatement.setLong(2, userId?:return false)
            preparedStatement.setString(3, nickname)
            return preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isUserRegistered error", e)
        }
        return false
    }

    fun deleteUser(userId: Long?) {
        try {
            if (userId == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("DELETE FROM " + config.mySQLTable + " WHERE user_id = ?;")
            preparedStatement.setLong(1, userId)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("deleteUserData error", e)
        }
    }

    fun updateUserData(userId: Long?, data: String?) {
        try {
            if (userId == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET data = ? WHERE user_id = ?;")
            preparedStatement.setString(1, data)
            preparedStatement.setLong(2, userId)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }
    fun updateUserAccountType(userId: Long?, accountType: Int?) {
        try {
            if (userId == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET account_type = ? WHERE user_id = ?;")
            preparedStatement.setInt(1, accountType?:3)
            preparedStatement.setLong(2, userId)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }
    fun updateUserTempArray(userId: Long?, tempArray: Array<String>?) {
        try {
            if (userId == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET temp_array = ? WHERE user_id = ?;")
            preparedStatement.setString(1, "{\"array\":[${tempArray?.joinToString(", ") { "\"$this\"" }}]}")
            preparedStatement.setLong(2, userId)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }
    fun addToUserTempArray(userId: Long?, value: String?) : Boolean {
        try {
            if (userId == null) return false
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET temp_array = json_as_array_append(temp_array, ?) WHERE user_id = ?;")
            preparedStatement.setString(1, value?:return false)
            preparedStatement.setLong(2, userId)
            preparedStatement.executeUpdate()
            return true
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
        return false
    }
    fun updateUserNickname(userId: Long?, nickname: String?) {
        try {
            if (userId == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET nickname = ? WHERE user_id = ?;")
            preparedStatement.setString(1, nickname)
            preparedStatement.setLong(2, userId)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }
    fun addUserSecondNickname(userId: Long?, nickname: String?) : Boolean {
        try {
            if (userId == null) return false
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET nicknames = json_as_array_append(nicknames, ?) WHERE user_id = ?;")
            preparedStatement.setString(1, nickname)
            preparedStatement.setLong(2, userId)
            preparedStatement.executeUpdate()
            return true
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
        return false
    }
    fun updateUserSecondNicknames(userId: Long?, nicknames: Array<String>?) {
        try {
            if (userId == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET nicknames = ? WHERE user_id = ?;")
            preparedStatement.setString(1, "{\"array\":[${nicknames?.joinToString(", ") { "\"$this\"" }}]}")
            preparedStatement.setLong(2, userId)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }

    fun getUserData(userId: Long?): String {
        try {
            reConnect()
            if (isUserRegistered(userId)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT data FROM " + config.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, userId!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return query.getString(1)
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
        }
        return ""
    }
    fun getUserAccountType(userId: Long?): Int {
        try {
            reConnect()
            if (isUserRegistered(userId)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT account_type FROM " + config.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, userId!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return query.getInt(1)
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
        }
        return 3
    }
    fun getUserTempArray(userId: Long?): Array<String>? {
        try {
            reConnect()
            if (isUserRegistered(userId)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT temp_array FROM " + config.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, userId!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return gson.fromJson(query.getString(1), ArrayData::class.java).array
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
        }
        return null
    }
    fun getUserNickname(userId: Long?): String? {
        try {
            reConnect()
            if (isUserRegistered(userId)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT nickname FROM " + config.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, userId!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return query.getString(1)
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
        }
        return ""
    }
    fun getUserSecondNicknames(userId: Long?): Array<String>? {
        try {
            reConnect()
            if (isUserRegistered(userId)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT nicknames FROM " + config.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, userId!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return gson.fromJson(query.getString(1), ArrayData::class.java).array
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
        }
        return null
    }
    fun getUserIdByNickname(nickname: String?): Long? {
        try {
            reConnect()
            if (isNicknameRegistered(nickname)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT user_id FROM " + config.mySQLTable + " WHERE nickname = ? OR json_as_array_contains(nicknames, ?);")
                preparedStatement.setString(1, nickname)
                val query = preparedStatement.executeQuery()
                if (!query.next()) return null
                return query.getLong(1)
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
        }
        return null
    }
    fun getUserIdByUserTempArrayMember(member: String?): Long? {
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT user_id FROM " + config.mySQLTable + " WHERE json_as_array_contains(temp_array, ?);")
            preparedStatement.setString(1, member?:return null)
            val query = preparedStatement.executeQuery()
            if (!query.next()) return null
            return query.getLong(1)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
        }
        return null
    }

    val getAllLinkedEntities: HashMap<Long, NewSQLEntity>
        get() {
            val linkedEntities = HashMap<Long, NewSQLEntity>()
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT user_id FROM " + config.mySQLTable + ";")
                val query = preparedStatement.executeQuery()
                while (query.next()) {
                    val userId = query.getLong(1)
                    linkedEntities[userId] = NewSQLEntity(this, userId)
                }
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("getAllData error", e)
            }
            return linkedEntities
        }
    fun getLinkedEntity(userId: Long?): NewSQLEntity? =
        if (userId != null && isUserRegistered(userId)) NewSQLEntity(this, userId) else null
}