package ru.kochkaev.zixamc.tgbridge.legecySQL

import com.google.gson.Gson
import com.mysql.cj.jdbc.exceptions.CommunicationsException
import ru.kochkaev.zixamc.tgbridge.Config
import ru.kochkaev.zixamc.tgbridge.ConfigManager
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.dataclassSQL.*
import java.sql.*

/**
 * @author NikitaCartes
 */
class MySQL {
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
//            LogDebug(String.format("connecting to %s", uri))
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
//                LogInfo("Database connection closed successfully.")
            }
        } catch (e: CommunicationsException) {
            ZixaMCTGBridge.logger.error("Can't connect to database while closing", e)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Database connection not closed", e)
        }
    }

    val isClosed: Boolean
        get() = MySQLConnection == null


    fun registerUser(user_id: Long?, nickname: String?, nicknames: Array<String>?, account_type: Int?, data: AccountData?): Boolean =
        registerUser(user_id, nickname, nicknames, account_type, Gson().toJson(data))
    fun registerUser(user_id: Long?, nickname: String?, nicknames: Array<String>?, account_type: Int?, data: String?): Boolean {
        try {
            reConnect()
            if (!isUserRegistered(user_id) && user_id != null && !isNicknameRegistered(nickname) && (nicknames?.any{isNicknameRegistered(it)} != true)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("INSERT INTO " + config.mySQLTable + " (user_id, nickname, nicknames, account_type, temp_array, data) VALUES (?, ?, ?, ?, ?, ?);")
//                val sql_array_nicknames = MySQLConnection!!.createArrayOf("text", nicknames)
                preparedStatement.setLong(1, user_id)
                preparedStatement.setString(2, nickname)
                preparedStatement.setString(3, "{\"array\":[${nicknames?.joinToString(", ") { "\"$this\"" }}]}")
                preparedStatement.setInt(4, account_type?:3)
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

    fun isUserRegistered(user_id: Long?): Boolean {
        try {
            if (user_id == null) return false
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM " + config.mySQLTable + " WHERE user_id = ?;")
            preparedStatement.setLong(1, user_id)
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
    fun isNicknameNotAvailableToRegister(user_id: Long?, nickname: String?): Boolean {
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM " + config.mySQLTable + " WHERE user_id = ? AND (nickname = ? OR json_as_array_contains(nicknames, ?));")
            preparedStatement.setString(1, nickname)
            preparedStatement.setLong(2, user_id?:return false)
            preparedStatement.setString(3, nickname)
            return preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isUserRegistered error", e)
        }
        return false
    }
//    fun isNicknamesRegistered(nicknames: Array<String>?): Boolean {
//        try {
//            reConnect()
//            if (nicknames == null) return false
//            var contains = false;
//            for (nickname in nicknames) {
//                val preparedStatement =
//                    MySQLConnection!!.prepareStatement("SELECT * FROM " + ConfigManager.CONFIG!!.mySQLTable + " WHERE nickname = ? OR ARRAY_CONTAINS(second_nicknames, ?);")
//                preparedStatement.setString(1, nickname)
//                preparedStatement.setString(2, nickname)
//                contains = preparedStatement.executeQuery().next()
//            }
//            return contains
//        } catch (e: SQLException) {
//            ZixaMCTGBridge.logger.error("isUserRegistered error", e)
//        }
//        return false
//    }

    fun deleteUser(user_id: Long?) {
        try {
            if (user_id == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("DELETE FROM " + config.mySQLTable + " WHERE user_id = ?;")
            preparedStatement.setLong(1, user_id)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("deleteUserData error", e)
        }
    }

    fun updateUserData(user_id: Long?, data: String?) {
        try {
            if (user_id == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET data = ? WHERE user_id = ?;")
            preparedStatement.setString(1, data)
            preparedStatement.setLong(2, user_id)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }
    fun updateUserAccountType(user_id: Long?, account_type: Int?) {
        try {
            if (user_id == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET account_type = ? WHERE user_id = ?;")
            preparedStatement.setInt(1, account_type?:3)
            preparedStatement.setLong(2, user_id)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }
    fun updateUserTempArray(user_id: Long?, temp_array: Array<String>?) {
        try {
            if (user_id == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET temp_array = ? WHERE user_id = ?;")
            preparedStatement.setString(1, "{\"array\":[${temp_array?.joinToString(", ") { "\"$this\"" }}]}")
            preparedStatement.setLong(2, user_id)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }
    fun addToUserTempArray(user_id: Long?, value: String?) : Boolean {
        try {
            if (user_id == null) return false
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET temp_array = json_as_array_append(temp_array, ?) WHERE user_id = ?;")
            preparedStatement.setString(1, value?:return false)
            preparedStatement.setLong(2, user_id)
            preparedStatement.executeUpdate()
            return true
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
        return false
    }
    fun updateUserNickname(user_id: Long?, nickname: String?) {
        try {
            if (user_id == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET nickname = ? WHERE user_id = ?;")
            preparedStatement.setString(1, nickname)
            preparedStatement.setLong(2, user_id)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }
    fun addUserSecondNickname(user_id: Long?, nickname: String?) : Boolean {
        try {
            if (user_id == null) return false
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET nicknames = json_as_array_append(nicknames, ?) WHERE user_id = ?;")
            preparedStatement.setString(1, nickname)
            preparedStatement.setLong(2, user_id)
            preparedStatement.executeUpdate()
            return true
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
        return false
    }
    fun updateUserSecondNicknames(user_id: Long?, nicknames: Array<String>?) {
        try {
            if (user_id == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + config.mySQLTable + " SET nicknames = ? WHERE user_id = ?;")
            preparedStatement.setString(1, "{\"array\":[${nicknames?.joinToString(", ") { "\"$this\"" }}]}")
            preparedStatement.setLong(2, user_id)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("updateUserData error", e)
        }
    }

    fun getUserData(user_id: Long?): String {
        try {
            reConnect()
            if (isUserRegistered(user_id)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT data FROM " + config.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, user_id!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return query.getString(1)
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
        }
        return ""
    }
    fun getUserAccountType(user_id: Long?): Int {
        try {
            reConnect()
            if (isUserRegistered(user_id)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT account_type FROM " + config.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, user_id!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return query.getInt(1)
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
        }
        return 3
    }
    fun getUserTempArray(user_id: Long?): Array<String>? {
        try {
            reConnect()
            if (isUserRegistered(user_id)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT temp_array FROM " + config.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, user_id!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return gson.fromJson(query.getString(1), ArrayData::class.java).array
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
        }
        return null
    }
    fun getUserNickname(user_id: Long?): String? {
        try {
            reConnect()
            if (isUserRegistered(user_id)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT nickname FROM " + config.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, user_id!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return query.getString(1)
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
        }
        return ""
    }
    fun getUserSecondNicknames(user_id: Long?): Array<String>? {
        try {
            reConnect()
            if (isUserRegistered(user_id)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT nicknames FROM " + config.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, user_id!!)
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

//    val allData: HashMap<Long, TableRow>
//        get() {
//            val registeredUsers = HashMap<Long, TableRow>()
//            try {
//                reConnect()
//                val preparedStatement =
//                    MySQLConnection!!.prepareStatement("SELECT * FROM " + config.mySQLTable + ";")
//                val query = preparedStatement.executeQuery()
//                while (query.next()) {
//                    val user_id = query.getLong(2)
//                    val nickname = query.getString(3)
//                    val second_nicknames = gson.fromJson(query.getString(4), ArrayData::class.java).array
//                    val account_type = query.getInt(5)
//                    val data: AccountData = MySQLIntegration.parseJsonToPOJO(query.getString(6), account_type)
//                    registeredUsers[user_id] = TableRow(nickname, second_nicknames, account_type, data)
//                }
//            } catch (e: SQLException) {
//                ZixaMCTGBridge.logger.error("getAllData error", e)
//            }
//            return registeredUsers
//        }
    val getAllLinkedEntities: HashMap<Long, SQLEntity>
        get() {
            val linkedEntities = HashMap<Long, SQLEntity>()
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT user_id FROM " + config.mySQLTable + ";")
                val query = preparedStatement.executeQuery()
                while (query.next()) {
                    val user_id = query.getLong(1)
                    linkedEntities[user_id] = SQLEntity(this, user_id)
                }
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("getAllData error", e)
            }
            return linkedEntities
        }
    fun getLinkedEntity(user_id: Long?): SQLEntity? = if (user_id != null && isUserRegistered(user_id)) SQLEntity(this, user_id) else null

//    fun saveAll(playerCacheMap: HashMap<Long?, TableRow?>) {
//        try {
//            reConnect()
//            val preparedStatement =
//                MySQLConnection!!.prepareStatement("INSERT INTO " + config.mySQLTable + " (user_id, nickname, second_nicknames, account_type, data) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE nickname = ?, second_nicknames = ?, account_type = ?, data = ?;")
//            // Updating player data.
//            playerCacheMap.forEach { (user_id: Long?, userInfo: TableRow?) ->
//                val nickname: String = userInfo?.nickname?:return@forEach
//                val account_type: Int = userInfo.account_type
//                val data: String = gson.toJson(userInfo.data)
//                try {
//                    preparedStatement.setLong(1, user_id?:return@forEach)
//                    preparedStatement.setString(2, nickname)
//                    preparedStatement.setString(3, "{\"array\":[${userInfo.second_nicknames?.joinToString(", ") { "\"$this\"" }}]}")
//                    preparedStatement.setInt(4, account_type)
//                    preparedStatement.setString(5, data)
//                    preparedStatement.setString(6, nickname)
//                    preparedStatement.setString(3, "{\"array\":[${userInfo.second_nicknames?.joinToString(", ") { "\"$this\"" }}]}")
//                    preparedStatement.setInt(8, account_type)
//                    preparedStatement.setString(9, data)
//
//                    preparedStatement.addBatch()
//                } catch (e: SQLException) {
//                    ZixaMCTGBridge.logger.error(String.format("Error saving player data! %s ", user_id))
//                }
//            }
//            preparedStatement.executeBatch()
//        } catch (e: SQLException) {
//            ZixaMCTGBridge.logger.error("Error saving players data", e)
//        } catch (e: NullPointerException) {
//            ZixaMCTGBridge.logger.error("Error saving players data", e)
//        }
//    }
}