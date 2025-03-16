package ru.kochkaev.zixamc.tgbridge.sql

import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.chatSync.ChatSyncBotCore
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager
import ru.kochkaev.zixamc.tgbridge.config.TextData
import ru.kochkaev.zixamc.tgbridge.config.serialize.TextDataAdapter
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.MySQLConnection
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.reConnect
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.GroupData
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
                chatId = config.chatId,
                name = "zixa",
                data = gson.toJson(GroupData(
                    topicId = config.topicId,
                    prefix = config.reply.defaultPrefix
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
                create(chatId, null, gson.toJson(GroupData()))
            return SQLGroup(chatId)
        }
        fun create(chatId: Long, name:String?, data:String?): Boolean {
            try {
                reConnect()
                if (!exists(chatId) && (name == null || !exists(name))) {
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("INSERT INTO $tableName (chat_id, name, data) VALUES (?, ?, ?);")
                    preparedStatement.setLong(1, chatId)
                    preparedStatement.setString(2, name)
                    preparedStatement.setString(3, data?:gson.toJson(GroupData()))
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
                    MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE name = ?;")
                preparedStatement.setString(1, name)
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
                        MySQLConnection!!.prepareStatement("SELECT chat_id FROM $tableName WHERE name = ?;")
                    preparedStatement.setString(1, name)
                    val query = preparedStatement.executeQuery()
                    query.next()
                    query.getLong(1)
                } else null
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("Register error ", e)
                null
            }
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
        ChatSyncBotCore.config.reply.prefixAppend.get(
            plainPlaceholders = listOf(
                "group" to name.toString(),
                "message_id" to messageId.toString()
            ),
            componentPlaceholders = listOf(
                "prefix" to (data.prefix?.get()?:Component.text(name.toString()))
            )
        )
    fun getResolvedFromMcPrefix(messageId: Int): Component =
        ChatSyncBotCore.config.reply.prefixAppend.get(
            plainPlaceholders = listOf(
                "group" to name.toString(),
                "message_id" to messageId.toString()
            ),
            componentPlaceholders = listOf(
                "prefix" to (data.fromMcPrefix?.get()?:data.prefix?.get()?:Component.text(name.toString()))
            )
        )
}