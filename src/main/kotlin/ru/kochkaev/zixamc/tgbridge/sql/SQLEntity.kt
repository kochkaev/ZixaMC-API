package ru.kochkaev.zixamc.tgbridge.sql

import com.google.gson.Gson
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.MySQLConnection
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.reConnect
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.*
import java.sql.SQLException

class SQLEntity private constructor(val userId: Long) {

    var nickname: String?
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT nickname FROM $tableName WHERE user_id = ?;")
            preparedStatement.setLong(1, userId)
            val query = preparedStatement.executeQuery()
            query.next()
            query.getString(1)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
            ""
        }
        set(nickname) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET nickname = ? WHERE user_id = ?;")
                preparedStatement.setString(1, nickname)
                preparedStatement.setLong(2, userId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("updateUserData error", e)
            }
        }
    var nicknames = SQLArray(SQLEntity, "nicknames", userId, "user_id")
    var accountType: AccountType
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT account_type FROM $tableName WHERE user_id = ?;")
            preparedStatement.setLong(1, userId)
            val query = preparedStatement.executeQuery()
            query.next()
            AccountType.parse(query.getInt(1))
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
            AccountType.UNKNOWN
        }
        set(accountType) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET account_type = ? WHERE user_id = ?;")
                preparedStatement.setInt(1, accountType.getId())
                preparedStatement.setLong(2, userId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("updateUserData error", e)
            }
        }
    val tempArray = SQLArray(SQLEntity, "temp_array", userId, "user_id")
    var agreedWithRules: Boolean
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT agreed_with_rules FROM $tableName WHERE user_id = ?;")
            preparedStatement.setLong(1, userId)
            val query = preparedStatement.executeQuery()
            query.next()
            query.getBoolean(1)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
            false
        }
        set(agreedWithRules) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET agreed_with_rules = ? WHERE user_id = ?;")
                preparedStatement.setBoolean(1, agreedWithRules)
                preparedStatement.setLong(2, userId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("updateUserData error", e)
            }
        }
    var isRestricted: Boolean
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT is_restricted FROM $tableName WHERE user_id = ?;")
            preparedStatement.setLong(1, userId)
            val query = preparedStatement.executeQuery()
            query.next()
            query.getBoolean(1)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
            false
        }
        set(isRestricted) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET is_restricted = ? WHERE user_id = ?;")
                preparedStatement.setBoolean(1, isRestricted)
                preparedStatement.setLong(2, userId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("updateUserData error", e)
            }
        }
    var data: AccountData?
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT data FROM $tableName WHERE user_id = ?;")
            preparedStatement.setLong(1, userId)
            val query = preparedStatement.executeQuery()
            query.next()
            gson.fromJson(query.getString(1), AccountData::class.java)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
            null
        }
        set(data) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET data = ? WHERE user_id = ?;")
                preparedStatement.setString(1, gson.toJson(data))
                preparedStatement.setLong(2, userId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("updateUserData error", e)
            }
        }

    companion object: MySQL() {
        override val tableName: String = config.usersTable
        override fun getModel(): String =
            String.format(
                """
                CREATE TABLE `%s`.`%s` (
                    `id` INT NOT NULL AUTO_INCREMENT,
                    `user_id` BIGINT NOT NULL,
                    `nickname` VARCHAR(16),
                    `nicknames` JSON DEFAULT "[]",
                    `account_type` INT NOT NULL DEFAULT 3,
                    `temp_array` JSON NOT NULL DEFAULT "[]",
                    `agreed_with_rules` BOOLEAN NOT NULL DEFAULT FALSE,
                    `is_restricted` BOOLEAN NOT NULL DEFAULT FALSE,
                    `data` JSON NOT NULL DEFAULT "{}",
                    PRIMARY KEY (`id`), UNIQUE (`user_id`)
                ) ENGINE = InnoDB;
                """.trimIndent(),
                config.database,
                config.usersTable
            )
        val gson = Gson()

        fun get(userId: Long) =
            if (exists(userId)) SQLEntity(userId)
            else null
        fun get(nickname: String) =
            if (exists(nickname))
                SQLEntity(getId(nickname)!!)
            else null
        fun getByTempArray(value: String): SQLEntity? {
            val userId: Long = getIdByTempArrayVal(value)?:return null
            return get(userId)
        }
        fun getOrCreate(userId: Long): SQLEntity {
            if (!exists(userId)) createDefault(userId)
            return SQLEntity(userId)
        }
        fun create(userId: Long, nickname: String?, nicknames: List<String>?, accountType: Int?, data: String?): Boolean {
            try {
                reConnect()
                if (!exists(userId) && (nickname == null || !exists(nickname)) && (nicknames?.any{ exists(it) } != true)) {
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("INSERT INTO $tableName (user_id, nickname, nicknames, account_type, temp_array, data) VALUES (?, ?, ?, ?, ?, ?);")
                    preparedStatement.setLong(1, userId)
                    preparedStatement.setString(2, nickname)
                    preparedStatement.setString(3, gson.toJson(nicknames?:listOf<String>()))
                    preparedStatement.setInt(4, accountType?:3)
                    preparedStatement.setString(5, gson.toJson(listOf<String>()))
                    preparedStatement.setString(6, data)
                    preparedStatement.executeUpdate()
                    return true
                }
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("Register error ", e)
            }
            return false
        }
        fun createDefault(userId: Long) =
            create(
                userId = userId,
                nickname = null,
                nicknames = listOf(),
                accountType = AccountType.UNKNOWN.getId(),
                data = gson.toJson(AccountData())
            )
        
        fun exists(userId: Long) = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE user_id = ?;")
            preparedStatement.setLong(1, userId)
            preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isRegistered error", e)
            false
        }
        fun exists(nickname: String) = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE nickname = ? OR JSON_CONTAINS(nicknames, JSON_QUOTE(?), '$');")
            preparedStatement.setString(1, nickname)
            preparedStatement.setString(2, nickname)
            preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isRegistered error", e)
            false
        }
        
        fun getId(nickname: String) = try {
            reConnect()
            if (exists(nickname)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT user_id FROM $tableName WHERE nickname = ? OR JSON_CONTAINS(nicknames, JSON_QUOTE(?), '$');")
                preparedStatement.setString(1, nickname)
                preparedStatement.setString(2, nickname)
                val query = preparedStatement.executeQuery()
                if (!query.next()) null
                else query.getLong(1)
            } else null
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
            null
        }
        fun getIdByTempArrayVal(value: String) = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT user_id FROM $tableName WHERE JSON_CONTAINS(temp_array, JSON_QUOTE(?), '$');")
            preparedStatement.setString(1, value)
            val query = preparedStatement.executeQuery()
            if (!query.next()) null
            else query.getLong(1)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
            null
        }

        val users: List<LinkedUser>
            get() {
                val users = arrayListOf<LinkedUser>()
                try {
                    reConnect()
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("SELECT user_id FROM $tableName;")
                    val query = preparedStatement.executeQuery()
                    while (query.next()) {
                        val userId = query.getLong(1)
                        users.add(LinkedUser(userId))
                    }
                } catch (e: SQLException) {
                    ZixaMCTGBridge.logger.error("getAllData error", e)
                }
                return users
            }
    }

    fun canTakeNickname(nickname: String) =
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE user_id != ? AND (nickname = ? OR JSON_CONTAINS(nicknames, JSON_QUOTE(?), '$'));")
            preparedStatement.setLong(1, userId)
            preparedStatement.setString(2, nickname)
            preparedStatement.setString(3, nickname)
            !preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isRegistered error", e)
            false
        }

    fun setPreferNickname(nickname: String) {
        if ((data?.minecraftAccounts?:return).stream().anyMatch{it.nickname == nickname}) {
            addNickname(nickname)
        }
    }
    fun addNickname(nickname: String) {
        if (!nicknames.contains(nickname)) nicknames.add(nickname)
        this.nickname = nickname
    }

    fun createAndOrGetData(): AccountData {
        if (data == null) data = AccountData()
        return data!!
    }

    fun addMinecraftAccount(account: MinecraftAccountData): Boolean {
        val accounts = createAndOrGetData().minecraftAccounts
        if (accounts.stream().anyMatch{it.nickname == account.nickname}) return false
        else accounts.add(account)
        if (!nicknames.contains(account.nickname)) nicknames.add(account.nickname)
        if (nickname == null) nickname = account.nickname
        data = data!!.apply { this.minecraftAccounts = accounts }
        return true
    }
    fun editMinecraftAccount(nickname: String, newStatus: MinecraftAccountType) {
        val accounts = (data?:return).minecraftAccounts
        val matched = accounts.first { it.nickname == nickname }
        matched.accountStatus = newStatus
        accounts.removeIf { it.nickname == nickname }
        accounts.add(matched)
        data = data!!.apply { this.minecraftAccounts = accounts }
    }
    fun addRequest(requestData: RequestData) {
        if (accountType == AccountType.UNKNOWN) accountType = AccountType.REQUESTER
        val requests = createAndOrGetData().requests
        requests.add(requestData)
        data = data!!.apply { this.requests = requests }
    }
    fun editRequest(requestData: RequestData) {
        val requests = (data?:return).requests
        requests.removeIf {it.user_request_id == requestData.user_request_id}
        requests.add(requestData)
//        when (requestData.request_status) {
//            "creating" -> {
//                requests.removeIf {it.request_status == "creating"}
//                requests.add(requestData)
//            }
//            else -> {
//                requests.removeIf {it.message_id_in_chat_with_user == requestData.message_id_in_chat_with_user}
//                requests.add(requestData)
//            }
//        }
        data = data!!.apply { this.requests = requests }
    }

    fun setProtectedInfoMessage(
        message: TgMessage,
        protectLevel: Int,
        protectedType: String,
        senderBotId: Long,
    ) {
        data = data?.apply { this.protectedMessages.add(
            ProtectedMessageData(
            message_id = message.messageId.toLong(),
            chat_id = message.chat.id,
            protect_level = protectLevel,
            protected_type = protectedType,
            sender_bot_id = senderBotId,
        )
        ) }
    }
}