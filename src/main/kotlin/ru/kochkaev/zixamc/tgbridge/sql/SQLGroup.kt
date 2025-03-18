package ru.kochkaev.zixamc.tgbridge.sql

import com.google.gson.GsonBuilder
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import org.objectweb.asm.Type
import ru.kochkaev.zixamc.tgbridge.ServerBot
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
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager.CONFIG
import ru.kochkaev.zixamc.tgbridge.config.TextData
import ru.kochkaev.zixamc.tgbridge.config.serialize.TextDataAdapter
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgEntity
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgReplyParameters
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.MySQLConnection
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.reConnect
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.GroupData
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.SQLArray
import java.sql.SQLException

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
    val members = SQLArray(SQLGroup, "members", chatId, "chat_id")
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
    var topicId: Int?
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT topic_id FROM $tableName WHERE chat_id = ?;")
            preparedStatement.setLong(1, chatId)
            val query = preparedStatement.executeQuery()
            query.next()
            val int = query.getInt(1)
            if (query.wasNull()) null
            else int
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("getUserData error", e)
            null
        }
        set(topicId) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET topic_id = ? WHERE chat_id = ?;")
                if (topicId != null)
                    preparedStatement.setInt(1, topicId)
                else preparedStatement.setNull(1, Type.INT)
                preparedStatement.setLong(2, chatId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("updateUserData error", e)
            }
        }
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
                    `aliases` JSON DEFAULT "{\"array\":[]}",
                    `members` JSON DEFAULT "{\"array\":[]}",
                    `agreed_with_rules` BOOLEAN NOT NULL DEFAULT FALSE,
                    `topic_id` INT DEFAULT NULL,
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
            .registerTypeAdapter(TextData::class.java, TextDataAdapter())
            .create()
        override fun afterCreateTable() {
            val config = ConfigManager.CONFIG!!.serverBot.chatSync
            create(
                chatId = config.defaultGroup.chatId,
                name = config.defaultGroup.name,
                aliases = config.defaultGroup.aliases.toTypedArray(),
                members = SQLEntity.userIDs.map { it.toString() } .toTypedArray(),
                agreedWithRules = true,
                topicId = config.defaultGroup.topicId,
                data = gson.toJson(GroupData(
                    prefix = config.defaultGroup.prefix,
                    fromMcPrefix = config.defaultGroup.fromMcPrefix
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
                create(chatId, null, arrayOf(), arrayOf(), false, null, gson.toJson(GroupData()))
            return SQLGroup(chatId)
        }
        fun create(chatId: Long, name:String?, aliases: Array<String>?, members: Array<String>?, agreedWithRules: Boolean, topicId: Int?, data:String?): Boolean {
            try {
                reConnect()
                if (!exists(chatId) && (name == null || !exists(name))) {
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("INSERT INTO $tableName (chat_id, name, aliases, members, agreed_with_rules, topic_id, data) VALUES (?, ?, ?, ?, ?, ?, ?);")
                    preparedStatement.setLong(1, chatId)
                    preparedStatement.setString(2, name)
                    preparedStatement.setString(3, "{\"array\":[${aliases?.joinToString(", ") { "\"$this\"" }}]}")
                    preparedStatement.setString(4, "{\"array\":[${members?.joinToString(", ") { "\"$this\"" }}]}")
                    preparedStatement.setBoolean(5, agreedWithRules)
                    if (topicId != null)
                        preparedStatement.setInt(6, topicId)
                    else preparedStatement.setNull(6, Type.INT)
                    preparedStatement.setString(7, data?:gson.toJson(GroupData()))
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
                    MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE name = ? OR JSON_CONTAINS(aliases, JSON_QUOTE(?), '$.array');")
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
                        MySQLConnection!!.prepareStatement("SELECT chat_id FROM $tableName WHERE name = ? OR JSON_CONTAINS(aliases, JSON_QUOTE(?), '$.array');")
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

        val all: HashMap<Long, SQLGroup>
            get() = chatIDs.fold(hashMapOf()) { acc, it ->
                acc[it] = get(it)!!
                acc
            }
        val chatIDs: List<Long>
            get() {
                val ids = arrayListOf<Long>()
                try {
                    reConnect()
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("SELECT chat_id FROM $tableName;")
                    val query = preparedStatement.executeQuery()
                    while (query.next()) {
                        val chatId = query.getLong(1)
                        ids.add(chatId)
                    }
                } catch (e: SQLException) {
                    ZixaMCTGBridge.logger.error("getAllData error", e)
                }
                return ids
            }
    }

    var lastMessage: LastMessage? = null
    val lastMessageLock = Mutex()

    fun canTakeName(value: String) =
        try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM ${SQLEntity.tableName} WHERE chat_id != ? AND (name = ? OR JSON_CONTAINS(aliases, JSON_QUOTE(?), '$.array'));")
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
                "prefix" to (data.prefix?.get()?:Component.text(name.toString()))
            )
        )
    fun getResolvedFromMcPrefix(messageId: Int): Component =
        config.lang.minecraft.prefixAppend.get(
            plainPlaceholders = listOf(
                "group" to name.toString(),
                "message_id" to messageId.toString()
            ),
            componentPlaceholders = listOf(
                "prefix" to (data.fromMcPrefix?.get()?:data.prefix?.get()?:Component.text(name.toString()))
            )
        )

    fun checkValidMsg(msg: TgMessage) = msg.let {
        enabled &&
        it.messageThreadId == topicId
    }
    val enabled: Boolean
        get() = agreedWithRules && data.enabledChatSync
    fun isMember(nickname: String) =
        members.contains(SQLEntity.get(nickname)?.userId.toString())

    suspend fun sendMessage(text: String, reply: Int? = null, entities: List<TgEntity>? = null): TgMessage {
        return bot.sendMessage(
            chatId = chatId,
            text = text,
            messageThreadId = topicId,
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
            replyToText(tgMessage, topicId, TextParser.resolveMessageLink(tgMessage), bot.me.id)?.also {
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
            output.append("<a href=\"tg://user?id=$it\">$placeholder</a>")
        }
        return output.toString()
    }
}