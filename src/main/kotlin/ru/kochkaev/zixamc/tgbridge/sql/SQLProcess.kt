package ru.kochkaev.zixamc.tgbridge.sql

import com.google.gson.GsonBuilder
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.config.TextData
import ru.kochkaev.zixamc.tgbridge.config.serialize.TextDataAdapter
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.*
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.MySQLConnection
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.reConnect
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup.Companion
import java.sql.SQLException
import java.util.Random

class SQLProcess<T: ProcessData> private constructor(
    val chatId: Long,
    val type: ProcessType<T>
) {
//    var type: ProcessType<T>?
//        get() = try {
//            reConnect()
//            val preparedStatement =
//                MySQLConnection!!.prepareStatement("SELECT type FROM $tableName WHERE chat_id = ?;")
//            preparedStatement.setLong(1, chatId)
//            val query = preparedStatement.executeQuery()
//            query.next()
//            val type = query.getString(1)
//            if (query.wasNull()) null
//            else gson.fromJson<ProcessType<T>>(type, ProcessType::class.java)
//        } catch (e: SQLException) {
//            ZixaMCTGBridge.logger.error("Register error ", e)
//            null
//        }
//        set(type) {
//            try {
//                reConnect()
//                val preparedStatement =
//                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET type = ? WHERE chat_id = ?;")
//                preparedStatement.setString(1, gson.toJson(type))
//                preparedStatement.setLong(2, chatId)
//                preparedStatement.executeUpdate()
//            } catch (e: SQLException) {
//                ZixaMCTGBridge.logger.error("Register error ", e)
//            }
//        }
    var data: T?
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT data FROM $tableName WHERE chat_id = ? AND type = ?;")
            preparedStatement.setLong(1, chatId)
            preparedStatement.setString(2, gson.toJson(type))
            val query = preparedStatement.executeQuery()
            query.next()
            gson.fromJson(query.getString(1), type.model)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Register error ", e)
            null
        }
        set(data) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET data = ? WHERE chat_id = ? AND type = ?;")
                preparedStatement.setString(1, gson.toJson(data))
                preparedStatement.setLong(2, chatId)
                preparedStatement.setString(2, gson.toJson(type))
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("Register error ", e)
            }
        }

    companion object: MySQL() {
        override val tableName: String = config.processesTable
        override fun getModel(): String =
            String.format(
                """
                CREATE TABLE `%s`.`%s` (
                    `id` INT NOT NULL AUTO_INCREMENT,
                    `chat_id` BIGINT NOT NULL,
                    `type` JSON DEFAULT NULL,
                    `data` JSON NOT NULL DEFAULT "{}",
                    PRIMARY KEY (`id`)
                ) ENGINE = InnoDB;
                """.trimIndent(),
                config.database,
                tableName
            )
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(TextData::class.java, TextDataAdapter())
            .create()

        fun <T: ProcessData> get(chatId: Long, type: ProcessType<T>) =
            if (exists(chatId, type))
                SQLProcess(chatId, type)
            else null
        fun getAll(chatId: Long): List<SQLProcess<*>> = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE chat_id = ?;")
            preparedStatement.setLong(1, chatId)
            val query = preparedStatement.executeQuery()
            val all = arrayListOf<SQLProcess<*>>()
            while (query.next())
                all.add(SQLProcess(query.getLong("chat_id"), gson.fromJson(query.getString("type"), ProcessType::class.java)))
            all
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isUserRegistered error", e)
            listOf()
        }
        fun <T: ProcessData> of(
            type: ProcessType<T>,
            data: T,
        ): Builder<T> = Builder(type, data)
        fun create(chatId: Long, type: ProcessType<*>, data:ProcessData?): Boolean {
            try {
                reConnect()
                if (!exists(chatId, type)) {
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("INSERT INTO $tableName (chat_id, type, data) VALUES (?, ?, ?);")
                    preparedStatement.setLong(1, chatId)
                    preparedStatement.setString(2, gson.toJson(type))
                    preparedStatement.setString(3, gson.toJson(data))
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
        fun exists(chatId: Long, type: ProcessType<*>) =
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE chat_id = ? AND type = ?;")
                preparedStatement.setLong(1, chatId)
                preparedStatement.setString(2, gson.toJson(type))
                preparedStatement.executeQuery().next()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("isUserRegistered error", e)
                false
            }
        class Builder<T: ProcessData> internal constructor(
            var type: ProcessType<T>,
            var data: T
        ) {
            fun pull(chatId: Long) {
                create(chatId, type, data)
            }
            fun with(mod: (T) -> T): Builder<T> =
                Builder(type, mod(data))
        }


        fun migrateChatId(oldChatId: Long, newChatId: Long) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET chat_id = ? WHERE chat_id = ?;")
                preparedStatement.setLong(1, newChatId)
                preparedStatement.setLong(2, oldChatId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("Register error ", e)
            }
        }
    }
    fun drop() {
        try {
            reConnect()
            if (exists(chatId)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("DELETE FROM $tableName WHERE chat_id = ? AND type = ?;")
                preparedStatement.setLong(1, chatId)
                preparedStatement.setString(2, gson.toJson(type))
                preparedStatement.executeUpdate()
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Register error ", e)
        }
    }
}