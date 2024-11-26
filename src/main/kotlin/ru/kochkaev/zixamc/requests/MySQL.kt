package ru.kochkaev.zixamc.requests

import com.google.gson.Gson
import com.mysql.cj.jdbc.exceptions.CommunicationsException
import org.checkerframework.checker.units.qual.A
import ru.kochkaev.zixamc.requests.dataclassSQL.*
import java.sql.*

/**
 * @author NikitaCartes
 */
class MySQL {
    private var MySQLConnection: Connection? = null
    val gson = Gson()

    /* Account types:
        0 -> Admin
        1 -> Player
        2 -> Requester
        3 -> Unknown
     */
    @Throws(Exception::class)
    fun connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val uri =
                "jdbc:mysql://" + ConfigManager.CONFIG!!.mySQLHost + "/" + ConfigManager.CONFIG!!.mySQLDatabase + "?autoReconnect=true"
//            LogDebug(String.format("connecting to %s", uri))
            MySQLConnection = DriverManager.getConnection(uri, ConfigManager.CONFIG!!.mySQLUser, ConfigManager.CONFIG!!.mySQLPassword)
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?;")
            preparedStatement.setString(1, ConfigManager.CONFIG!!.mySQLTable)
            if (!preparedStatement.executeQuery().next()) {
                MySQLConnection!!.createStatement().executeUpdate(
                    java.lang.String.format(
                        """
                                        CREATE TABLE `%s`.`%s` (
                                            `id` INT NOT NULL AUTO_INCREMENT,
                                            `user_id` BIGINT NOT NULL,
                                            `nickname` VARCHAR(16),
                                            `second_nicknames` VARCHAR(16)[],
                                            `account_type` INT NOT NULL,
                                            `data` JSON NOT NULL,
                                            PRIMARY KEY (`id`), UNIQUE (`user_id`)
                                        ) ENGINE = InnoDB;
                                        """.trimIndent(),
                        ConfigManager.CONFIG!!.mySQLDatabase,
                        ConfigManager.CONFIG!!.mySQLTable
                    )
                )
            }
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
                ZixaMCRequests.logger.debug("Reconnecting to MySQL")
                if (MySQLConnection != null) {
                    MySQLConnection!!.close()
                }
                connect()
            }
        } catch (e: Exception) {
            ZixaMCRequests.logger.error("Mysql reconnect failed", e)
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("Mysql reconnect failed", e)
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
            ZixaMCRequests.logger.error("Can't connect to database while closing", e)
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("Database connection not closed", e)
        }
    }

    val isClosed: Boolean
        get() = MySQLConnection == null


    fun registerUser(user_id: Long?, nickname: String?, second_nicknames: Array<String>?, account_type: Int?, data: AccountData?): Boolean =
        registerUser(user_id, nickname, second_nicknames, account_type, Gson().toJson(data))
    fun registerUser(user_id: Long?, nickname: String?, second_nicknames: Array<String>?, account_type: Int?, data: String?): Boolean {
        try {
            reConnect()
            if (!isUserRegistered(user_id) && user_id != null && !isNicknameRegistered(nickname) && !isNicknamesRegistered(second_nicknames)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("INSERT INTO " + ConfigManager.CONFIG!!.mySQLTable + " (user_id, nickname, second_nickname, account_type, data) VALUES (?, ?, ?, ?, ?);")
                val sql_array_second_nicknames = MySQLConnection!!.createArrayOf("text", second_nicknames)
                preparedStatement.setLong(1, user_id)
                preparedStatement.setString(2, nickname)
                preparedStatement.setArray(3, sql_array_second_nicknames)
                preparedStatement.setInt(4, account_type?:3)
                preparedStatement.setString(5, data)
                preparedStatement.executeUpdate()
                return true
            }
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("Register error ", e)
        }
        return false
    }

    fun isUserRegistered(user_id: Long?): Boolean {
        try {
            if (user_id == null) return false
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM " + ConfigManager.CONFIG!!.mySQLTable + " WHERE user_id = ?;")
            preparedStatement.setLong(1, user_id)
            return preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("isUserRegistered error", e)
        }
        return false
    }
    fun isNicknameRegistered(nickname: String?): Boolean {
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM " + ConfigManager.CONFIG!!.mySQLTable + " WHERE nickname = ? OR second_nickname = ?;")
            preparedStatement.setString(1, nickname)
            preparedStatement.setString(2, nickname)
            return preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("isUserRegistered error", e)
        }
        return false
    }
    fun isNicknamesRegistered(nicknames: Array<String>?): Boolean {
        try {
            reConnect()
            if (nicknames == null) return false
            var contains = false;
            for (nickname in nicknames) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT * FROM " + ConfigManager.CONFIG!!.mySQLTable + " WHERE nickname = ? OR ARRAY_CONTAINS(second_nicknames, ?);")
                preparedStatement.setString(1, nickname)
                preparedStatement.setString(2, nickname)
                contains = preparedStatement.executeQuery().next()
            }
            return contains
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("isUserRegistered error", e)
        }
        return false
    }

    fun deleteUser(user_id: Long?) {
        try {
            if (user_id == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("DELETE FROM " + ConfigManager.CONFIG!!.mySQLTable + " WHERE user_id = ?;")
            preparedStatement.setLong(1, user_id)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("deleteUserData error", e)
        }
    }

    fun updateUserData(user_id: Long?, data: String?) {
        try {
            if (user_id == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + ConfigManager.CONFIG!!.mySQLTable + " SET data = ? WHERE user_id = ?;")
            preparedStatement.setString(1, data)
            preparedStatement.setLong(1, user_id)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("updateUserData error", e)
        }
    }
    fun updateUserAccountType(user_id: Long?, account_type: Int?) {
        try {
            if (user_id == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + ConfigManager.CONFIG!!.mySQLTable + " SET account_type = ? WHERE user_id = ?;")
            preparedStatement.setInt(1, account_type?:3)
            preparedStatement.setLong(1, user_id)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("updateUserData error", e)
        }
    }
    fun updateUserNickname(user_id: Long?, nickname: String?) {
        try {
            if (user_id == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + ConfigManager.CONFIG!!.mySQLTable + " SET nickname = ? WHERE user_id = ?;")
            preparedStatement.setString(1, nickname)
            preparedStatement.setLong(1, user_id)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("updateUserData error", e)
        }
    }
    fun addUserSecondNickname(user_id: Long?, second_nickname: String?) : Boolean {
        try {
            if (user_id == null) return false
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + ConfigManager.CONFIG!!.mySQLTable + " SET second_nicknames = ARRAY_APPEND(second_nicknames, ?) WHERE user_id = ?;")
            preparedStatement.setString(1, second_nickname)
            preparedStatement.setLong(1, user_id)
            preparedStatement.executeUpdate()
            return true
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("updateUserData error", e)
        }
        return false
    }
    fun updateUserSecondNicknames(user_id: Long?, second_nicknames: Array<String>?) {
        try {
            if (user_id == null) return
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("UPDATE " + ConfigManager.CONFIG!!.mySQLTable + " SET second_nicknames = ? WHERE user_id = ?;")
            preparedStatement.setArray(1, MySQLConnection!!.createArrayOf("text", second_nicknames))
            preparedStatement.setLong(1, user_id)
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("updateUserData error", e)
        }
    }

    fun getUserData(user_id: Long?): String {
        try {
            reConnect()
            if (isUserRegistered(user_id)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT data FROM " + ConfigManager.CONFIG!!.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, user_id!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return query.getString(1)
            }
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("getUserData error", e)
        }
        return ""
    }
    fun getUserAccountType(user_id: Long?): Int {
        try {
            reConnect()
            if (isUserRegistered(user_id)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT account_type FROM " + ConfigManager.CONFIG!!.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, user_id!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return query.getInt(1)
            }
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("getUserData error", e)
        }
        return 3
    }
    fun getUserNickname(user_id: Long?): String {
        try {
            reConnect()
            if (isUserRegistered(user_id)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT nickname FROM " + ConfigManager.CONFIG!!.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, user_id!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return query.getString(1)
            }
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("getUserData error", e)
        }
        return ""
    }
    fun getUserSecondNicknames(user_id: Long?): Array<String>? {
        try {
            reConnect()
            if (isUserRegistered(user_id)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT second_nicknames FROM " + ConfigManager.CONFIG!!.mySQLTable + " WHERE user_id = ?;")
                preparedStatement.setLong(1, user_id!!)
                val query = preparedStatement.executeQuery()
                query.next()
                return query.getArray(1).array as Array<String>
            }
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("getUserData error", e)
        }
        return null
    }
    fun getUserIdByNickname(nickname: String?): Int? {
        try {
            reConnect()
            if (isNicknameRegistered(nickname)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT user_id FROM " + ConfigManager.CONFIG!!.mySQLTable + " WHERE nickname = ? OR ARRAY_CONTAINS(second_nicknames, ?);")
                preparedStatement.setString(1, nickname)
                val query = preparedStatement.executeQuery()
                if (!query.next()) return null
                return query.getInt(1)
            }
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("getUserData error", e)
        }
        return null
    }

    val allData: HashMap<Long, TableRow>
        get() {
            val registeredUsers = HashMap<Long, TableRow>()
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT * FROM " + ConfigManager.CONFIG!!.mySQLTable + ";")
                val query = preparedStatement.executeQuery()
                while (query.next()) {
                    val user_id = query.getLong(2)
                    val nickname = query.getString(3)
                    val second_nicknames = query.getArray(4).array as Array<String>
                    val account_type = query.getInt(5)
                    val data: AccountData = MySQLIntegration.parseJsonToPOJO(query.getString(6), account_type)
                    registeredUsers[user_id] = TableRow(nickname, second_nicknames, account_type, data)
                }
            } catch (e: SQLException) {
                ZixaMCRequests.logger.error("getAllData error", e)
            }
            return registeredUsers
        }
    val getAllLinkedEntities: HashMap<Long, SQLEntity>
        get() {
            val linkedEntities = HashMap<Long, SQLEntity>()
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT user_id FROM " + ConfigManager.CONFIG!!.mySQLTable + ";")
                val query = preparedStatement.executeQuery()
                while (query.next()) {
                    val user_id = query.getLong(1)
                    linkedEntities[user_id] = SQLEntity(this, user_id)
                }
            } catch (e: SQLException) {
                ZixaMCRequests.logger.error("getAllData error", e)
            }
            return linkedEntities
        }
    fun getLinkedEntity(user_id: Long?): SQLEntity? = if (user_id != null && isUserRegistered(user_id)) SQLEntity(this, user_id) else null

    fun saveAll(playerCacheMap: HashMap<Long?, TableRow?>) {
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("INSERT INTO " + ConfigManager.CONFIG!!.mySQLTable + " (user_id, nickname, second_nicknames, account_type, data) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE nickname = ?, second_nicknames = ?, account_type = ?, data = ?;")
            // Updating player data.
            playerCacheMap.forEach { (user_id: Long?, userInfo: TableRow?) ->
                val nickname: String = userInfo?.nickname?:return@forEach
                val second_nicknames = MySQLConnection!!.createArrayOf("text", userInfo.second_nicknames)
                val account_type: Int = userInfo.account_type
                val data: String = gson.toJson(userInfo.data)
                try {
                    preparedStatement.setLong(1, user_id?:return@forEach)
                    preparedStatement.setString(2, nickname)
                    preparedStatement.setArray(3, second_nicknames)
                    preparedStatement.setInt(4, account_type)
                    preparedStatement.setString(5, data)
                    preparedStatement.setString(6, nickname)
                    preparedStatement.setArray(7, second_nicknames)
                    preparedStatement.setInt(8, account_type)
                    preparedStatement.setString(9, data)

                    preparedStatement.addBatch()
                } catch (e: SQLException) {
                    ZixaMCRequests.logger.error(String.format("Error saving player data! %s ", user_id))
                }
            }
            preparedStatement.executeBatch()
        } catch (e: SQLException) {
            ZixaMCRequests.logger.error("Error saving players data", e)
        } catch (e: NullPointerException) {
            ZixaMCRequests.logger.error("Error saving players data", e)
        }
    }
}