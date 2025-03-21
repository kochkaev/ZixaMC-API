package ru.kochkaev.zixamc.tgbridge.sql

import com.google.gson.GsonBuilder
import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.ServerBot.coroutineScope
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore.config
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotLogic
import ru.kochkaev.zixamc.tgbridge.chatSync.LastMessage
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.MinecraftAdventureConverter
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser.replyToText
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager.CONFIG
import ru.kochkaev.zixamc.tgbridge.config.TextData
import ru.kochkaev.zixamc.tgbridge.config.serialize.TextDataAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.TopicTypeAdapter
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgEntity
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgEntityType
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.MySQLConnection
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.reConnect
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.*
import java.sql.SQLException
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.TopicTypes.CHAT_SYNC
import java.util.ArrayList

class SQLGroup private constructor(val chatId: Long) {

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
    val aliases = SQLArray(SQLGroup, "aliases", chatId, "chat_id")
    val members = SQLUsersArray(SQLGroup, "members", chatId, "chat_id")
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
    val topics = SQLTopicsMap(SQLGroup, "topics", chatId, "chat_id")
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
                    `name` VARCHAR(16),
                    `aliases` JSON DEFAULT "[]",
                    `members` JSON DEFAULT "[]",
                    `agreed_with_rules` BOOLEAN NOT NULL DEFAULT FALSE,
                    `is_restricted` BOOLEAN NOT NULL DEFAULT FALSE,
                    `topics` JSON NOT NULL DEFAULT "{}",
                    `data` JSON NOT NULL DEFAULT "{}",
                    PRIMARY KEY (`id`), UNIQUE (`chat_id`)
                ) ENGINE = InnoDB;
                """.trimIndent(),
                config.database,
                config.groupsTable
            )
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(TextData::class.java, TextDataAdapter())
            .registerTypeAdapter(Topic::class.java, TopicTypeAdapter())
            .create()
        override fun afterCreateTable() {
            val config = CONFIG!!.serverBot.chatSync
            create(
                chatId = config.defaultGroup.chatId,
                name = config.defaultGroup.name,
                aliases = config.defaultGroup.aliases,
                members = SQLEntity.users.map { it.key.toString() },
                agreedWithRules = true,
                isRestricted = false,
                topics = mapOf(
                    CHAT_SYNC to ChatSyncTopicData(
                        enabled = true,
                        topicId = config.defaultGroup.topicId,
//                        name = config.defaultGroup.name,
//                        aliases = ArrayList(config.defaultGroup.aliases),
                        prefix = config.defaultGroup.prefix,
                        fromMcPrefix = config.defaultGroup.fromMcPrefix
                    )
                ),
                data = gson.toJson(GroupData())
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
                create(chatId, null, listOf(), listOf(), false, false, mapOf(), gson.toJson(GroupData()))
            return SQLGroup(chatId)
        }
        fun create(chatId: Long, name:String?, aliases: List<String>?, members: List<String>?, agreedWithRules: Boolean, isRestricted: Boolean, topics: Map<Topic<out TopicData>, TopicData>, data:String?): Boolean {
            try {
                reConnect()
                if (!exists(chatId) && (name == null || !exists(name))) {
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("INSERT INTO $tableName (chat_id, name, aliases, members, agreed_with_rules, is_restricted, topics, data) VALUES (?, ?, ?, ?, ?, ?, ?, ?);")
                    preparedStatement.setLong(1, chatId)
                    preparedStatement.setString(2, name)
                    preparedStatement.setString(3, gson.toJson(aliases?:listOf<String>()))
                    preparedStatement.setString(4, gson.toJson(members?:listOf<String>()))
                    preparedStatement.setBoolean(5, agreedWithRules)
                    preparedStatement.setBoolean(6, isRestricted)
                    preparedStatement.setString(7, gson.toJson(topics))
                    preparedStatement.setString(8, data?:gson.toJson(GroupData()))
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
//                    MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE JSON_CONTAINS_PATH(topics, 'one', '$.map.CHAT_SYNC') AND (topics->>'$.map.CHAT_SYNC.name' = ? OR JSON_CONTAINS(topics, JSON_QUOTE(?), '$.map.CHAT_SYNC.aliases'));")
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
//                        MySQLConnection!!.prepareStatement("SELECT chat_id FROM $tableName WHERE JSON_CONTAINS_PATH(topics, 'one', '$.map.CHAT_SYNC') AND (topics->>'$.map.CHAT_SYNC.name' = ? OR JSON_CONTAINS(topics, JSON_QUOTE(?), '$.map.CHAT_SYNC.aliases'));")
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

        fun collectData(chatId: Long, userId: Long?) {
            if (chatId>0 || userId == null) return
            val group = get(chatId)?:return
            if (!SQLEntity.exists(userId))
                SQLEntity.createDefault(userId)
            if (!group.members.contains(userId))
                group.members.add(userId)
        }
    }

    var lastMessage: LastMessage? = null
    val lastMessageLock = Mutex()

    fun canTakeName(value: String) =
        try {
            reConnect()
            val preparedStatement =
//                MySQLConnection!!.prepareStatement("SELECT * FROM ${SQLEntity.tableName} WHERE chat_id != ? AND JSON_CONTAINS_PATH(topics, 'one', '$.map.CHAT_SYNC') AND (topics->>'$.map.CHAT_SYNC.name' = ? OR JSON_CONTAINS(topics, JSON_QUOTE(?), '$.map.CHAT_SYNC.aliases'));")
                MySQLConnection!!.prepareStatement("SELECT * FROM ${SQLEntity.tableName} WHERE chat_id != ? AND (name = ? OR JSON_CONTAINS(aliases, JSON_QUOTE(?), '$');")
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
            if (exists(chatId) && !exists(newChatId)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET chat_id = ? WHERE chat_id = ?;")
                preparedStatement.setLong(1, chatId)
                preparedStatement.setLong(2, newChatId)
                preparedStatement.executeUpdate()
                SQLGroup(newChatId)
            } else this
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Register error ", e)
            this
        }

    fun getResolvedPrefix(messageId: Int): Component =
        config.lang.minecraft.prefixAppend.get(
            plainPlaceholders = listOf(
                "group" to name.toString(),
                "message_id" to messageId.toString()
            ),
            componentPlaceholders = listOf(
                "prefix" to topics.getCasted(CHAT_SYNC).let {
                    it?.prefix?.get() ?: Component.text(name.toString())
                }
            )
        )
    fun getResolvedFromMcPrefix(messageId: Int): Component =
        config.lang.minecraft.prefixAppend.get(
            plainPlaceholders = listOf(
                "group" to name.toString(),
                "message_id" to messageId.toString()
            ),
            componentPlaceholders = listOf(
                "prefix" to (topics.getCasted(CHAT_SYNC).let {
                    it?.fromMcPrefix?.get() ?: it?.prefix?.get() ?: Component.text(name.toString())
                } )
            )
        )

    fun checkValidMsg(msg: TgMessage) = msg.let {
        enabled &&
        enabledChatSync &&
        it.messageThreadId == topics.getCasted(CHAT_SYNC)?.topicId
    }
    val enabled: Boolean
        get() = agreedWithRules
    val enabledChatSync: Boolean
        get() = topics.contains(CHAT_SYNC) && topics.getCasted(CHAT_SYNC)!!.enabled
    fun isMember(nickname: String) =
        members.contains(SQLEntity.get(nickname))

    suspend fun sendMessage(text: String, reply: Int? = null, entities: List<TgEntity>? = null): TgMessage {
        return bot.sendMessage(
            chatId = chatId,
            text = text,
            messageThreadId = topics.getCasted(CHAT_SYNC)!!.topicId,
            entities = entities,
            replyParameters = reply?.let { TgReplyParameters(reply) }
        )
    }
    suspend fun editMessageText(messageId: Int, text: String, entities: List<TgEntity>? = null): TgMessage {
        return bot.editMessageText(chatId, messageId, text, entities=entities)
    }
    suspend fun deleteMessage(messageId: Int) {
        bot.deleteMessage(chatId, messageId)
    }

    suspend fun broadcastMinecraft(
        nickname: String,
        message: String,
        replyTo: Int? = null,
    ): BroadcastMinecraftResult {
        if (!enabled || !isMember(nickname)) return BroadcastMinecraftResult.NOT_FOUND
        val tgMessage = ChatSyncBotLogic.sendReply(message, this, nickname, replyTo)
        if (tgMessage != null) {
            val messages = mutableListOf<Component>()
            var mcMessage = message
            replyToText(tgMessage, topics.getCasted(CHAT_SYNC)!!.topicId, TextParser.resolveMessageLink(tgMessage), bot.me.id)?.also {
                if (!config.messages.replyInDifferentLine) mcMessage = "$it $mcMessage"
                else messages.add(it).also { messages.add(Component.text("\n")) }
            }
            messages.add(
                config.lang.minecraft.messageMCFormat.get(
                    plainPlaceholders = listOf(
                        "nickname" to nickname,
                    ),
                    componentPlaceholders = listOf(
                        "text" to MinecraftAdventureConverter.minecraftToAdventure(
                            MarkdownLiteParserV1.ALL.parseNode(mcMessage).toText()
                        ),
                        "prefix" to getResolvedFromMcPrefix(tgMessage.messageId),
                    )
                )
            )
            ChatSyncBotCore.broadcastMessage(
                messages
                    .fold(Component.text()) { acc, component -> acc.append(component) }
                    .build(),
                this
            )
            return BroadcastMinecraftResult.SUCCESS
        } else return BroadcastMinecraftResult.TG_ERROR
    }
    enum class BroadcastMinecraftResult {
        SUCCESS,
        NOT_FOUND,
        TG_ERROR,
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
}