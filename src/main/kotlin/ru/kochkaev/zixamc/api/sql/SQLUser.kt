package ru.kochkaev.zixamc.api.sql

import com.google.gson.annotations.JsonAdapter
import kotlinx.coroutines.launch
import ru.kochkaev.zixamc.api.Initializer
import ru.kochkaev.zixamc.api.ZixaMC
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.config.serialize.SQLUserAdapter
import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataType
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataTypes
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.data.MinecraftAccountData
import ru.kochkaev.zixamc.api.sql.data.MinecraftAccountType
import ru.kochkaev.zixamc.api.sql.util.AbstractSQLField
import ru.kochkaev.zixamc.api.sql.util.BooleanSQLField
import ru.kochkaev.zixamc.api.sql.util.NullableStringSQLField
import ru.kochkaev.zixamc.api.sql.util.StringSQLArray
import ru.kochkaev.zixamc.api.sql.feature.FeatureTypes
import ru.kochkaev.zixamc.api.sql.util.ChatDataSQLMap
import ru.kochkaev.zixamc.api.telegram.BotLogic
import ru.kochkaev.zixamc.api.telegram.RulesManager
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup
import java.sql.SQLException

@JsonAdapter(SQLUserAdapter::class)
class SQLUser private constructor(id: Long): SQLChat(id) {

    private val nicknameField = NullableStringSQLField(SQLUser, "nickname", id, "user_id")
    var nickname: String?
        get() = nicknameField.get()
        set(nickname) { nicknameField.set(nickname) }
    val nicknames = StringSQLArray(SQLUser, "nicknames", id, "user_id")
    private val accountTypeField = object: AbstractSQLField<AccountType>(SQLUser, "account_type", id, "user_id", AccountType::class.java,
        getter = { rs -> AccountType.parse(rs.getInt(1)) },
        setter = { ps, it -> ps.setInt(1, it.id) }
    ) {
        override fun set(value: AccountType): Boolean {
            val original = super.set(value)
            if (original && !value.isHigherThanOrEqual(AccountType.PLAYER)) Initializer.coroutineScope.launch {
                SQLGroup.getAllWithUser(id).forEach { chat ->
                    if (chat.features.getCasted(FeatureTypes.PLAYERS_GROUP)?.autoRemove == true)
                        for (it in BotLogic.bots) try {
                            it.banChatMember(chat.id, id)
                            it.sendMessage(
                                chatId = chat.id,
                                text = ConfigManager.config.general.rules.onLeave4group.formatLang("nickname" to (nickname?:""))
                            )
                            break
                        } catch (_: Exception) {}
                    if (!chat.atLeastOnePlayer()) chat.onNoMorePlayers()
                }
                data.getCasted(ChatDataTypes.MINECRAFT_ACCOUNTS)
                    ?.filter { MinecraftAccountType.getAllActiveNow().contains(it.accountStatus) }
                    ?.forEach { editMinecraftAccount(it.nickname, MinecraftAccountType.FROZEN) }
                deleteProtected(AccountType.PLAYER)
            }
            return original
        }
    }
    var accountType: AccountType
        get() = accountTypeField.get() ?: AccountType.UNKNOWN
        set(accountType) { accountTypeField.set(accountType) }
    val tempArray = StringSQLArray(SQLUser, "temp_array", id, "user_id")
    private val agreedWithRulesField = BooleanSQLField(SQLUser, "agreed_with_rules", id, "user_id")
    var agreedWithRules: Boolean
        get() = agreedWithRulesField.get() ?: false
        set(agreedWithRules) { agreedWithRulesField.set(agreedWithRules) }
    private val isRestrictedField = BooleanSQLField(SQLUser, "is_restricted", id, "user_id")
    var isRestricted: Boolean
        get() = isRestrictedField.get() ?: false
        set(isRestricted) { isRestrictedField.set(isRestricted) }
    override val data = ChatDataSQLMap(SQLUser, "data", id, "user_id")

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

        fun get(userId: Long) =
            if (exists(userId)) SQLUser(userId)
            else null
        @Deprecated("Not safe", replaceWith = ReplaceWith("get(userId)"))
        fun getWithoutCheck(userId: Long) = SQLUser(userId)
        fun get(nickname: String) =
            if (exists(nickname))
                SQLUser(getId(nickname)!!)
            else null
        fun getByTempArray(value: String): SQLUser? {
            val userId: Long = getIdByTempArrayVal(value)?:return null
            return get(userId)
        }
        fun getOrCreate(userId: Long): SQLUser {
            if (!exists(userId)) createDefault(userId)
            return SQLUser(userId)
        }
        fun create(userId: Long, nickname: String?, nicknames: List<String>?, accountType: Int?, data: Map<ChatDataType<*>, *> = mapOf<ChatDataType<*>, Any>()): Boolean {
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
                    preparedStatement.setString(6, gson.toJson(data))
                    preparedStatement.executeUpdate()
                    return true
                }
            } catch (e: SQLException) {
                ZixaMC.logger.error("Register error ", e)
            }
            return false
        }
        fun createDefault(userId: Long) =
            create(
                userId = userId,
                nickname = null,
                nicknames = listOf(),
                accountType = AccountType.UNKNOWN.id,
            )
        
        fun exists(userId: Long) = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE user_id = ?;")
            preparedStatement.setLong(1, userId)
            preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMC.logger.error("isRegistered error", e)
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
            ZixaMC.logger.error("isRegistered error", e)
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
            ZixaMC.logger.error("getUserData error", e)
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
            ZixaMC.logger.error("getUserData error", e)
            null
        }

        val users: List<SQLUser>
            get() {
                val users = arrayListOf<SQLUser>()
                try {
                    reConnect()
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("SELECT user_id FROM $tableName;")
                    val query = preparedStatement.executeQuery()
                    while (query.next()) {
                        val userId = query.getLong(1)
                        users.add(SQLUser(userId))
                    }
                } catch (e: SQLException) {
                    ZixaMC.logger.error("getAllData error", e)
                }
                return users
            }
    }

    fun isInTable() = exists(id)
    fun addToTable() = createDefault(id)

    fun canTakeNickname(nickname: String) =
        try {
            MySQL.reConnect()
            val preparedStatement =
                MySQL.MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE user_id != ? AND (nickname = ? OR JSON_CONTAINS(nicknames, JSON_QUOTE(?), '$'));")
            preparedStatement.setLong(1, id)
            preparedStatement.setString(2, nickname)
            preparedStatement.setString(3, nickname)
            !preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMC.logger.error("isRegistered error", e)
            false
        }

    fun setPreferNickname(nickname: String) {
        if (data.getCasted(ChatDataTypes.MINECRAFT_ACCOUNTS)?.any { it.nickname == nickname } == true) {
            addNickname(nickname)
        }
    }
    fun addNickname(nickname: String) {
        if (!nicknames.contains(nickname)) nicknames.add(nickname)
        this.nickname = nickname
    }

    fun addMinecraftAccount(account: MinecraftAccountData): Boolean {
        val accounts = data.getCasted(ChatDataTypes.MINECRAFT_ACCOUNTS) ?: arrayListOf()
        if (accounts.stream().anyMatch{it.nickname == account.nickname}) return false
        else accounts.add(account)
        if (!nicknames.contains(account.nickname)) nicknames.add(account.nickname)
        if (nickname == null) nickname = account.nickname
        data.set(ChatDataTypes.MINECRAFT_ACCOUNTS, accounts)
        return true
    }
    fun editMinecraftAccount(nickname: String, newStatus: MinecraftAccountType) {
        val accounts = data.getCasted(ChatDataTypes.MINECRAFT_ACCOUNTS) ?: return
        val matched = accounts.first { it.nickname == nickname }
        matched.accountStatus = newStatus
        accounts.removeIf { it.nickname == nickname }
        accounts.add(matched)
        data.set(ChatDataTypes.MINECRAFT_ACCOUNTS, accounts)
    }

    override suspend fun hasProtectedLevel(level: AccountType): Boolean =
        accountType.isHigherThanOrEqual(level)

    override suspend fun sendRulesUpdated(capital: Boolean) {
        val message = ConfigManager.config.general.rules.updated4player
        val menu = TgMenu(
            listOf(
                listOf(
                    SQLCallback.of(
                        display = if (capital) ConfigManager.config.general.rules.agreeButton
                            else ConfigManager.config.general.rules.removeButton,
                        type = "rules",
                        data = RulesManager.RulesCallbackData(
                            operation = if (capital) RulesManager.RulesOperation.SET_AGREE
                                else RulesManager.RulesOperation.REMOVE_AGREE,
                            type = RulesManager.RulesOperationType.RULES_UPDATED
                        ),
                        canExecute = ServerBotGroup.CAN_EXECUTE_OWNER
                    )
                )))
        for(it in BotLogic.bots) {
            try {
                it.sendMessage(
                    chatId = id,
                    text = message,
                    replyMarkup = menu,
                )
                break
            } catch (_: Exception) { }
        }
        if (capital) agreedWithRules = false
    }
}