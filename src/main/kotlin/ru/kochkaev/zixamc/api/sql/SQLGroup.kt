package ru.kochkaev.zixamc.api.sql

import com.google.gson.annotations.JsonAdapter
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.api.Initializer.coroutineScope
import ru.kochkaev.zixamc.api.ZixaMC
import ru.kochkaev.zixamc.chatsync.LastMessage
import ru.kochkaev.zixamc.api.config.ConfigManager.config
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.config.serialize.SQLGroupAdapter
import ru.kochkaev.zixamc.api.sql.callback.TgMenu
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataType
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataTypes
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.util.BooleanSQLField
import ru.kochkaev.zixamc.api.sql.util.FeaturesSQLMap
import ru.kochkaev.zixamc.api.sql.util.NullableStringSQLField
import ru.kochkaev.zixamc.api.sql.util.StringSQLArray
import ru.kochkaev.zixamc.api.sql.util.UsersSQLArray
import ru.kochkaev.zixamc.api.telegram.BotLogic
import ru.kochkaev.zixamc.api.telegram.ServerBot
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup
import ru.kochkaev.zixamc.api.sql.feature.data.FeatureData
import ru.kochkaev.zixamc.api.sql.feature.FeatureType
import ru.kochkaev.zixamc.api.sql.feature.FeatureTypes
import java.sql.SQLException
import ru.kochkaev.zixamc.api.sql.util.ChatDataSQLMap
import ru.kochkaev.zixamc.api.telegram.RulesManager
import ru.kochkaev.zixamc.api.telegram.model.TgChat
import ru.kochkaev.zixamc.api.telegram.model.TgUser

@JsonAdapter(SQLGroupAdapter::class)
class SQLGroup private constructor(val chatId: Long): SQLChat(chatId) {

    private val nameField = NullableStringSQLField(SQLGroup, "name", chatId, "chat_id")
    var name: String?
        get() = nameField.get()
        set(name) { nameField.set(name) }
    val aliases = StringSQLArray(SQLGroup, "aliases", chatId, "chat_id")
    val members = UsersSQLArray(SQLGroup, "members", chatId, "chat_id")
    private val agreedWithRulesField = BooleanSQLField(SQLGroup, "agree_with_rules", chatId, "chat_id")
    var agreedWithRules: Boolean
        get() = agreedWithRulesField.get() ?: false
        set(agreedWithRules) { agreedWithRulesField.set(agreedWithRules) }
    private val isRestrictedField = BooleanSQLField(SQLGroup, "is_restricted", chatId, "chat_id")
    var isRestricted: Boolean
        get() = isRestrictedField.get() ?: false
        set(isRestricted) { isRestrictedField.set(isRestricted) }
    val features = FeaturesSQLMap(SQLGroup, "features", chatId, "chat_id", this)
    override val data = ChatDataSQLMap(SQLGroup, "data", chatId, "chat_id")

    companion object: MySQL() {
        override val tableName: String = config.groupsTable
        override fun getModel(): String =
            String.format(
                """
                CREATE TABLE `%s`.`%s` (
                    `id` INT NOT NULL AUTO_INCREMENT,
                    `chat_id` BIGINT NOT NULL,
                    `name` VARCHAR(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
                    `aliases` JSON NOT NULL DEFAULT "[]",
                    `members` JSON NOT NULL DEFAULT "[]",
                    `agreed_with_rules` BOOLEAN NOT NULL DEFAULT FALSE,
                    `is_restricted` BOOLEAN NOT NULL DEFAULT FALSE,
                    `features` JSON NOT NULL DEFAULT "{}",
                    `data` JSON NOT NULL DEFAULT "{}",
                    PRIMARY KEY (`id`), UNIQUE (`chat_id`)
                ) ENGINE = InnoDB;
                """.trimIndent(),
                config.database,
                config.groupsTable
            )

        fun get(chatId: Long) =
            if (exists(chatId)) SQLGroup(chatId)
            else null
        @Deprecated("Not safe", replaceWith = ReplaceWith("get(chatId)"))
        fun getWithoutCheck(chatId: Long) = SQLGroup(chatId)
        fun get(name: String): SQLGroup? {
            return SQLGroup(getId(name)?:return null)
        }
        fun getOrCreate(chatId: Long): SQLGroup {
            if (!exists(chatId))
                create(chatId, null, listOf(), listOf(), false, false, mapOf())
            return SQLGroup(chatId)
        }
        fun getAllWithUser(userId: Long) = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT chat_id FROM $tableName WHERE JSON_CONTAINS(members, JSON_QUOTE(?), '$');")
            preparedStatement.setString(1, userId.toString())
            val query = preparedStatement.executeQuery()
            val groups = arrayListOf<SQLGroup>()
            while (query.next())
                get(query.getLong(1))?.also { groups.add(it) }
            groups
        } catch (e: SQLException) {
            ZixaMC.logger.error("Register error ", e)
            listOf()
        }
        fun getAllWithFeature(feature: FeatureType<out FeatureData>) = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT chat_id FROM $tableName WHERE JSON_CONTAINS_PATH(currency_ids, 'one', '$.${feature.serializedName}');")
            val query = preparedStatement.executeQuery()
            val groups = arrayListOf<SQLGroup>()
            while (query.next())
                get(query.getLong(1))?.also { groups.add(SQLGroup(it.id)) }
            groups
        } catch (e: SQLException) {
            ZixaMC.logger.error("getGroupData error", e)
            listOf()
        }
        fun create(chatId: Long, name:String?, aliases: List<String>?, members: List<String>?, agreedWithRules: Boolean, isRestricted: Boolean, features: Map<FeatureType<out FeatureData>, FeatureData>, data: Map<ChatDataType<*>, *> = mapOf<ChatDataType<*>, Any>()): Boolean {
            try {
                reConnect()
                if (!exists(chatId) && (name == null || !exists(name))) {
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("INSERT INTO $tableName (chat_id, name, aliases, members, agreed_with_rules, is_restricted, features, data) VALUES (?, ?, ?, ?, ?, ?, ?, ?);")
                    preparedStatement.setLong(1, chatId)
                    preparedStatement.setString(2, name)
                    preparedStatement.setString(3, gson.toJson(aliases?:listOf<String>()))
                    preparedStatement.setString(4, gson.toJson(members?:listOf<String>()))
                    preparedStatement.setBoolean(5, agreedWithRules)
                    preparedStatement.setBoolean(6, isRestricted)
                    preparedStatement.setString(7, gson.toJson(features))
                    preparedStatement.setString(8, gson.toJson(data))
                    preparedStatement.executeUpdate()
                    return true
                }
            } catch (e: SQLException) {
                ZixaMC.logger.error("Register error ", e)
            }
            return false
        }

        fun exists(chatId: Long) =
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE chat_id = ?;")
                preparedStatement.setLong(1, chatId)
                preparedStatement.executeQuery().next()
            } catch (e: SQLException) {
                ZixaMC.logger.error("isUserRegistered error", e)
                false
            }
        fun exists(name: String) =
            try {
                reConnect()
                val preparedStatement =
//                    MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE JSON_CONTAINS_PATH(features, 'one', '$.map.CHAT_SYNC') AND (features->>'$.map.CHAT_SYNC.name' = ? OR JSON_CONTAINS(features, JSON_QUOTE(?), '$.map.CHAT_SYNC.aliases'));")
                    MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE name = ? OR JSON_CONTAINS(aliases, JSON_QUOTE(?), '$');")
                preparedStatement.setString(1, name)
                preparedStatement.setString(2, name)
                preparedStatement.executeQuery().next()
            } catch (e: SQLException) {
                ZixaMC.logger.error("isUserRegistered error", e)
                false
            }

        fun getId(name:String): Long? =
            try {
                reConnect()
                if (exists(name)) {
                    val preparedStatement =
//                        MySQLConnection!!.prepareStatement("SELECT chat_id FROM $tableName WHERE JSON_CONTAINS_PATH(features, 'one', '$.map.CHAT_SYNC') AND (features->>'$.map.CHAT_SYNC.name' = ? OR JSON_CONTAINS(features, JSON_QUOTE(?), '$.map.CHAT_SYNC.aliases'));")
                        MySQLConnection!!.prepareStatement("SELECT chat_id FROM $tableName WHERE name = ? OR JSON_CONTAINS(aliases, JSON_QUOTE(?), '$');")
                    preparedStatement.setString(1, name)
                    preparedStatement.setString(2, name)
                    val query = preparedStatement.executeQuery()
                    query.next()
                    query.getLong(1)
                } else null
            } catch (e: SQLException) {
                ZixaMC.logger.error("Register error ", e)
                null
            }

        val groups: List<SQLGroup>
            get() {
                val groups = arrayListOf<SQLGroup>()
                try {
                    reConnect()
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("SELECT chat_id FROM $tableName;")
                    val query = preparedStatement.executeQuery()
                    while (query.next()) {
                        val chatId = query.getLong(1)
                        groups.add(SQLGroup(chatId))
                    }
                } catch (e: SQLException) {
                    ZixaMC.logger.error("getAllData error", e)
                }
                return groups
            }

        suspend fun collectData(chat: TgChat, user: TgUser?) {
            if (chat.id>0 || user == null) return
            val userId = user.id
            val group = get(chat.id)?:return
            if (!SQLUser.exists(userId) && !user.isBot)
                SQLUser.createDefault(userId)
            if (!group.members.contains(userId))
                group.members.add(userId)
            if (chat.username != null && group.data.getCasted(ChatDataTypes.IS_PRIVATE)?:false) {
                group.data.set(ChatDataTypes.IS_PRIVATE, false)
                group.deleteProtected(AccountType.UNKNOWN)
                bot.sendMessage(
                    chatId = chat.id,
                    text = ServerBot.config.group.switchToPublic,
                )
            }
            else if (chat.username == null && !(group.data.getCasted(ChatDataTypes.IS_PRIVATE)?:false)) {
                group.data.set(ChatDataTypes.IS_PRIVATE, true)
            }
        }
    }

    var lastMessage: LastMessage? = null
    val lastMessageLock = Mutex()

    fun canTakeName(value: String) =
        try {
            MySQL.reConnect()
            val preparedStatement =
//                MySQLConnection!!.prepareStatement("SELECT * FROM ${SQLEntity.tableName} WHERE chat_id != ? AND JSON_CONTAINS_PATH(features, 'one', '$.map.CHAT_SYNC') AND (features->>'$.map.CHAT_SYNC.name' = ? OR JSON_CONTAINS(features, JSON_QUOTE(?), '$.map.CHAT_SYNC.aliases'));")
                MySQL.MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE chat_id != ? AND (name = ? OR JSON_CONTAINS(aliases, JSON_QUOTE(?), '$'));")
            preparedStatement.setLong(1, chatId)
            preparedStatement.setString(2, value)
            preparedStatement.setString(3, value)
            !preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMC.logger.error("isRegistered error", e)
            false
        }

    fun updateChatId(newChatId: Long) =
        try {
            MySQL.reConnect()
            val old = chatId
            if (exists(old) && !exists(newChatId)) {
                val preparedStatement =
                    MySQL.MySQLConnection!!.prepareStatement("UPDATE $tableName SET chat_id = ? WHERE chat_id = ?;")
                preparedStatement.setLong(1, newChatId)
                preparedStatement.setLong(2, old)
                preparedStatement.executeUpdate()
                SQLProcess.migrateChatId(old, newChatId)
                SQLCallback.migrateChatId(old, newChatId)
                SQLGroup(newChatId)
            } else this
        } catch (e: SQLException) {
            ZixaMC.logger.error("Register error ", e)
            this
        }

    val enabled: Boolean
        get() = agreedWithRules
    fun isMember(nickname: String) =
        SQLUser.get(nickname)?.let { members.contains(it) } ?: false
    suspend fun getNoBotsMembers(): List<SQLUser> =
        members.get()?.let {
            it.filter { it1 -> !bot.getChatMember(chatId, it1.id).user.isBot }
        } ?: listOf()

    fun withScopeAndLock(fn: suspend () -> Unit) {
        coroutineScope.launch {
            lastMessageLock.withLock {
                fn()
            }
        }
    }

    fun mentionAll() : String {
        val output = StringBuilder()
        val placeholder = config.serverBot.mentionAllReplaceWith
        members.get()?.forEach {
            output.append("<a href=\"tg://user?id=${it.id}\">$placeholder</a>")
        }
        return output.toString()
    }


    override suspend fun hasProtectedLevel(level: AccountType): Boolean =
        getNoBotsMembers().fold(true) { acc, it -> acc && it.hasProtectedLevel(level) }
        && (!level.requireGroupPrivate || bot.getChat(chatId).username == null)
        && bot.getChatMemberCount(chatId) == (members.get()?.size?:0)+1
    override suspend fun deleteProtected(protectLevel: AccountType) {
        super.deleteProtected(protectLevel)
        features.getAll()?.keys?.forEach { key ->
            if (!key.checkAvailable(this)) features.remove(key)
        }
    }
    suspend fun atLeastOnePlayer() =
        members.get()?.fold(false) { acc, it -> acc || it.hasProtectedLevel(AccountType.PLAYER) } == true
    suspend fun onNoMorePlayers() {
        try {
            deleteProtected(AccountType.UNKNOWN)
        } catch (_: Exception) {}
        try {
            bot.sendMessage(
                chatId = chatId,
                text = ServerBot.config.group.hasNoMorePlayers,
            )
            bot.leaveChat(chatId)
        } catch (_: Exception) {}
    }

    override suspend fun sendRulesUpdated(capital: Boolean) {
        val isPlayers = features.contains(FeatureTypes.PLAYERS_GROUP)
        val message = if (isPlayers) config.general.rules.updated4group
            else config.general.rules.updated4player
        val menu = TgMenu(
            listOf(
            listOf(
                if (isPlayers) SQLCallback.of(
                    display = if (capital) config.general.rules.agreeButton
                        else config.general.rules.removeButton,
                    type = "rules",
                    data = RulesManager.RulesCallbackData(
                        operation = if (capital) RulesManager.RulesOperation.SET_AGREE
                            else RulesManager.RulesOperation.REMOVE_AGREE,
                        type = RulesManager.RulesOperationType.RULES_UPDATED
                    ),
                )
                else SQLCallback.of(
                    display = if (capital) config.general.rules.agreeButton
                        else config.general.rules.removeButton,
                    type = "rules",
                    data = RulesManager.RulesCallbackData(
                        operation = if (capital) RulesManager.RulesOperation.SET_AGREE_GROUP
                            else RulesManager.RulesOperation.REMOVE_AGREE_GROUP,
                        type = RulesManager.RulesOperationType.RULES_UPDATED
                    ),
                    canExecute = ServerBotGroup.CAN_EXECUTE_OWNER
                )
        )))
        for(it in BotLogic.bots) {
            try {
                it.sendMessage(
                    chatId = chatId,
                    text = message,
                    replyMarkup = menu,
                )
                break
            } catch (_: Exception) { }
        }
        if (capital) agreedWithRules = false
    }
}