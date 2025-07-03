package ru.kochkaev.zixamc.tgbridge.sql

import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.Initializer.coroutineScope
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.ChatSyncBotLogic
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.LastMessage
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager.CONFIG
import ru.kochkaev.zixamc.tgbridge.config.GsonManager.gson
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgEntity
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgMessage
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.MySQLConnection
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.reConnect
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgMenu
import ru.kochkaev.zixamc.tgbridge.sql.data.AccountType
import ru.kochkaev.zixamc.tgbridge.sql.data.ChatData
import ru.kochkaev.zixamc.tgbridge.sql.data.GroupData
import ru.kochkaev.zixamc.tgbridge.sql.util.*
import ru.kochkaev.zixamc.tgbridge.telegram.BotLogic
import ru.kochkaev.zixamc.tgbridge.telegram.RequestsBot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.ChatSyncFeatureData
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.FeatureData
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureType
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureTypes
import java.sql.SQLException
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureTypes.CHAT_SYNC
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.PlayersGroupFeatureData
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgChat
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgUser
import ru.kochkaev.zixamc.tgbridge.telegram.requests.RequestsBotUpdateManager

class SQLGroup private constructor(val chatId: Long): SQLChat(chatId) {

    var name: String?
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT name FROM $tableName WHERE chat_id = ?;")
            preparedStatement.setLong(1, chatId)
            val query = preparedStatement.executeQuery()
            query.next()
            query.getString(1)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Register error ", e)
            ""
        }
        set(name) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET name = ? WHERE chat_id = ?;")
                preparedStatement.setString(1, name)
                preparedStatement.setLong(2, chatId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("Register error ", e)
            }
        }
    val aliases = StringSQLArray(SQLGroup, "aliases", chatId, "chat_id")
    val members = UsersSQLArray(SQLGroup, "members", chatId, "chat_id")
    var agreedWithRules: Boolean
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT agreed_with_rules FROM $tableName WHERE chat_id = ?;")
            preparedStatement.setLong(1, chatId)
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
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET agreed_with_rules = ? WHERE chat_id = ?;")
                preparedStatement.setBoolean(1, agreedWithRules)
                preparedStatement.setLong(2, chatId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("updateUserData error", e)
            }
        }
    var isRestricted: Boolean
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT is_restricted FROM $tableName WHERE chat_id = ?;")
            preparedStatement.setLong(1, chatId)
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
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET agreed_with_rules = ? WHERE chat_id = ?;")
                preparedStatement.setBoolean(1, agreedWithRules)
                preparedStatement.setLong(2, chatId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("updateUserData error", e)
            }
        }
    val features = FeaturesSQLMap(SQLGroup, "features", chatId, "chat_id", this)
    var data: GroupData
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT data FROM $tableName WHERE chat_id = ?;")
            preparedStatement.setLong(1, chatId)
            val query = preparedStatement.executeQuery()
            query.next()
            gson.fromJson(query.getString(1), GroupData::class.java)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Register error ", e)
            GroupData()
        }
        set(data) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET data = ? WHERE chat_id = ?;")
                preparedStatement.setString(1, gson.toJson(data))
                preparedStatement.setLong(2, chatId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("Register error ", e)
            }
        }

    companion object: MySQL() {
        override val tableName: String = config.groupsTable
        override fun getModel(): String =
            String.format(
                """
                CREATE TABLE `%s`.`%s` (
                    `id` INT NOT NULL AUTO_INCREMENT,
                    `chat_id` BIGINT NOT NULL,
                    `name` VARCHAR(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
                    `aliases` JSON DEFAULT "[]",
                    `members` JSON DEFAULT "[]",
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
        override fun afterCreateTable() {
            val config = CONFIG!!.serverBot.chatSync
            create(
                chatId = config.defaultGroup.chatId,
                name = config.defaultGroup.name,
                aliases = config.defaultGroup.aliases,
                members = SQLUser.users.map { it.key.toString() },
                agreedWithRules = true,
                isRestricted = false,
                features = mapOf(
                    CHAT_SYNC to ChatSyncFeatureData(
                        enabled = true,
                        topicId = config.defaultGroup.topicId,
//                        name = config.defaultGroup.name,
//                        aliases = ArrayList(config.defaultGroup.aliases),
                        prefix = config.defaultGroup.prefix,
                        fromMcPrefix = config.defaultGroup.fromMcPrefix,
                        group = null
                    ),
                    FeatureTypes.PLAYERS_GROUP to PlayersGroupFeatureData(
                        autoAccept = true,
                        autoRemove = true,
                        group = null,
                    )
                ),
                data = gson.toJson(GroupData(
                    isPrivate = true,
                    greetingEnable = false
                ))
            )
        }

        fun get(chatId: Long) =
            if (exists(chatId)) SQLGroup(chatId)
            else null
        fun get(name: String): SQLGroup? {
            return SQLGroup(getId(name)?:return null)
        }
        fun getOrCreate(chatId: Long): SQLGroup {
            if (!exists(chatId))
                create(chatId, null, listOf(), listOf(), false, false, mapOf(), gson.toJson(ChatData()))
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
            ZixaMCTGBridge.logger.error("Register error ", e)
            listOf()
        }
        fun create(chatId: Long, name:String?, aliases: List<String>?, members: List<String>?, agreedWithRules: Boolean, isRestricted: Boolean, features: Map<FeatureType<out FeatureData>, FeatureData>, data:String?): Boolean {
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
                    preparedStatement.setString(8, data?:gson.toJson(ChatData()))
                    preparedStatement.executeUpdate()
                    return true
                }
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("Register error ", e)
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
                ZixaMCTGBridge.logger.error("isUserRegistered error", e)
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
                ZixaMCTGBridge.logger.error("isUserRegistered error", e)
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
                ZixaMCTGBridge.logger.error("Register error ", e)
                null
            }

        val groups: List<LinkedGroup>
            get() {
                val groups = arrayListOf<LinkedGroup>()
                try {
                    reConnect()
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("SELECT chat_id FROM $tableName;")
                    val query = preparedStatement.executeQuery()
                    while (query.next()) {
                        val chatId = query.getLong(1)
                        groups.add(LinkedGroup(chatId))
                    }
                } catch (e: SQLException) {
                    ZixaMCTGBridge.logger.error("getAllData error", e)
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
            val data = group.data
            if (chat.username != null && data.isPrivate) {
                group.data = data.apply {
                    this.isPrivate = false
                }
                group.cleanUpProtected(AccountType.UNKNOWN)
                bot.sendMessage(
                    chatId = chat.id,
                    text = ServerBot.config.integration.group.switchToPublic,
                )
            }
            else if (chat.username == null && !data.isPrivate) {
                group.data = data.apply {
                    this.isPrivate = true
                }
            }
        }
    }

    var lastMessage: LastMessage? = null
    val lastMessageLock = Mutex()

    fun canTakeName(value: String) =
        try {
            reConnect()
            val preparedStatement =
//                MySQLConnection!!.prepareStatement("SELECT * FROM ${SQLEntity.tableName} WHERE chat_id != ? AND JSON_CONTAINS_PATH(features, 'one', '$.map.CHAT_SYNC') AND (features->>'$.map.CHAT_SYNC.name' = ? OR JSON_CONTAINS(features, JSON_QUOTE(?), '$.map.CHAT_SYNC.aliases'));")
                MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE chat_id != ? AND (name = ? OR JSON_CONTAINS(aliases, JSON_QUOTE(?), '$'));")
            preparedStatement.setLong(1, chatId)
            preparedStatement.setString(2, value)
            preparedStatement.setString(3, value)
            !preparedStatement.executeQuery().next()
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isRegistered error", e)
            false
        }

    fun updateChatId(newChatId: Long) =
        try {
            reConnect()
            val old = chatId
            if (exists(old) && !exists(newChatId)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET chat_id = ? WHERE chat_id = ?;")
                preparedStatement.setLong(1, newChatId)
                preparedStatement.setLong(2, old)
                preparedStatement.executeUpdate()
                SQLProcess.migrateChatId(old, newChatId)
                SQLCallback.migrateChatId(old, newChatId)
                SQLGroup(newChatId)
            } else this
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Register error ", e)
            this
        }

    val enabled: Boolean
        get() = agreedWithRules
    fun isMember(nickname: String) =
        members.contains(SQLUser.get(nickname))
    suspend fun getNoBotsMembers(): List<LinkedUser> =
        members.get()?.let {
            it.filter { it1 -> !bot.getChatMember(chatId, it1.key).user.isBot }
        } ?: listOf()

    suspend fun sendMessage(text: String, reply: Int? = null, entities: List<TgEntity>? = null): TgMessage {
        return bot.sendMessage(
            chatId = chatId,
            text = text,
            messageThreadId = features.getCasted(CHAT_SYNC)!!.topicId,
            entities = entities,
            replyParameters = reply?.let {
                ru.kochkaev.zixamc.tgbridge.telegram.model.TgReplyParameters(
                    reply
                )
            }
        )
    }
    suspend fun editMessageText(messageId: Int, text: String, entities: List<TgEntity>? = null): TgMessage {
        return bot.editMessageText(chatId, messageId, text, entities=entities)
    }
    suspend fun deleteMessage(messageId: Int) {
        bot.deleteMessage(chatId, messageId)
    }

    fun withScopeAndLock(fn: suspend () -> Unit) {
        coroutineScope.launch {
            lastMessageLock.withLock {
                fn()
            }
        }
    }

    fun mentionAll() : String {
        val output = StringBuilder()
        val placeholder = CONFIG?.serverBot?.mentionAllReplaceWith?:"+"
        members.get()?.forEach {
            output.append("<a href=\"tg://user?id=${it.key}\">$placeholder</a>")
        }
        return output.toString()
    }


    override val dataGetter: () -> ChatData = { data }
    override val dataSetter: (ChatData) -> Unit = { data = it as GroupData }
    override suspend fun hasProtectedLevel(level: AccountType): Boolean =
        getNoBotsMembers().fold(true) { acc, it -> acc && it.getSQL()?.hasProtectedLevel(level) == true }
        && (!level.requireGroupPrivate || bot.getChat(chatId).username == null)
        && bot.getChatMemberCount(chatId) == (members.get()?.size?:0)+1
    suspend fun cleanUpProtected(protectLevel: AccountType) {
        protectLevel.levelHigh?.also { deleteProtected(it) }
        features.getAll()?.keys?.forEach { key ->
            if (!key.checkAvailable(this)) features.remove(key)
        }
    }
    suspend fun atLeastOnePlayer() =
        members.get()?.fold(false) { acc, it -> acc || it.getSQL()?.hasProtectedLevel(AccountType.PLAYER) == true } == true
    suspend fun onNoMorePlayers() {
        try {
            deleteProtected(AccountType.UNKNOWN)
        } catch (_: Exception) {}
        try {
            bot.sendMessage(
                chatId = chatId,
                text = ServerBot.config.integration.group.hasNoMorePlayers,
            )
            bot.leaveChat(chatId)
        } catch (_: Exception) {}
    }

    suspend fun sendRulesUpdated(capital: Boolean = false) {
        val isMain = ChatSyncBotLogic.DEFAULT_GROUP.chatId == chatId
        val message = BotLogic.escapePlaceholders(
            if (isMain)
                RequestsBot.config.target.lang.event.onRulesUpdated
            else ServerBot.config.integration.group.rulesUpdated)
        val menu = TgMenu(listOf(listOf(
            ServerBot.config.integration.group
                .let { if (capital) it.needAgreeWithRules else it.removeAgreeWithRules }
                .let {
                    if (isMain) SQLCallback.of(
                        display = it,
                        type = "requests",
                        data = RequestsBotUpdateManager.RequestCallback(
                            if (capital) RequestsBotUpdateManager.Operations.AGREE_WITH_RULES
                            else RequestsBotUpdateManager.Operations.REVOKE_AGREE_WITH_RULES
                        ),
                        canExecute = ServerBotGroup.CAN_EXECUTE_OWNER
                    )
                    else SQLCallback.of(
                        display = it,
                        type = "group",
                        data = ServerBotGroup.GroupCallback(
                            if (capital) ServerBotGroup.Operations.AGREE_WITH_RULES
                            else ServerBotGroup.Operations.REMOVE_AGREE_WITH_RULES
                        ),
                        canExecute = ServerBotGroup.CAN_EXECUTE_OWNER
                    )
                }
        )))
        try {
            RequestsBot.bot.sendMessage(
                chatId = chatId,
                text = message,
                replyMarkup = menu,
            )
        } catch (_: Exception) {
            try {
                bot.sendMessage(
                    chatId = chatId,
                    text = message,
                    replyMarkup = menu,
                )
            } catch (_: Exception) {}
        }
        if (capital) agreedWithRules = false
    }
}