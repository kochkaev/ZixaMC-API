package ru.kochkaev.zixamc.tgbridge.sql

import com.google.gson.GsonBuilder
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.config.TextData
import ru.kochkaev.zixamc.tgbridge.config.serialize.CallbackDataAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.TextDataAdapter
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.*
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.CallbackCanExecute
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCallback
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.MySQLConnection
import ru.kochkaev.zixamc.tgbridge.sql.MySQL.Companion.reConnect
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.*
import java.sql.SQLException
import java.util.Random

class SQLCallback<T:CallbackData> private constructor(
    val callbackId: Long,
    private val model: Class<T>,
) {
    val linked = SQLCallbacksArray(SQLCallback, "linked", callbackId, "callback_id")
    var canExecute: CallbackCanExecute?
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT can_execute FROM $tableName WHERE callback_id = ?;")
            preparedStatement.setLong(1, callbackId)
            val query = preparedStatement.executeQuery()
            query.next()
            gson.fromJson(query.getString(1), CallbackCanExecute::class.java)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Register error ", e)
            null
        }
        set(canExecute) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET can_execute = ? WHERE callback_id = ?;")
                preparedStatement.setString(1, gson.toJson(canExecute))
                preparedStatement.setLong(2, callbackId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("Register error ", e)
            }
        }
    var data: T?
        get() = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT data FROM $tableName WHERE callback_id = ?;")
            preparedStatement.setLong(1, callbackId)
            val query = preparedStatement.executeQuery()
            query.next()
            gson.fromJson<TgCallback<T>>(query.getString(1), TgCallback::class.java).data
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Register error ", e)
            null
        }
        set(data) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE $tableName SET data = ? WHERE callback_id = ?;")
                preparedStatement.setString(1, gson.toJson(TgCallback(type, data)))
                preparedStatement.setLong(2, callbackId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("Register error ", e)
            }
        }
    val type: String
        get() = resolve(callbackId)!!

    companion object: MySQL() {
        val registries: HashMap<String, Class<out CallbackData>> = hashMapOf("dummy" to CallbackData::class.java)
        override val tableName: String = config.callbacksTable
        override fun getModel(): String =
            String.format(
                """
                CREATE TABLE `%s`.`%s` (
                    `id` INT NOT NULL AUTO_INCREMENT,
                    `callback_id` BIGINT NOT NULL,
                    `linked` JSON NOT NULL DEFAULT "[]",
                    `can_execute` JSON NOT NULL DEFAULT "{}",
                    `data` JSON NOT NULL DEFAULT "{}",
                    PRIMARY KEY (`id`), UNIQUE (`callback_id`)
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
            .registerTypeAdapter(TgCallback::class.java, CallbackDataAdapter(registries))
            .create()
        private val random = Random()

        fun get(callbackId: Long) =
            if (exists(callbackId))
                registries[resolve(callbackId)]?.let {
                    SQLCallback(callbackId, it)
                }
            else null
        fun <T: CallbackData> of(
            display: String,
            type: String,
            data: T,
            canExecute: CallbackCanExecute = CallbackCanExecute(),
        ): Builder<T> {
            if (!registries.contains(type))
                registries[type] = data.javaClass
            return Builder(display, type, data, canExecute)
        }
        fun create(callbackId: Long, canExecute: String?, data:String?): Boolean {
            try {
                reConnect()
                if (!exists(callbackId)) {
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("INSERT INTO $tableName (callback_id, can_execute, data) VALUES (?, ?, ?);")
                    preparedStatement.setLong(1, callbackId)
                    preparedStatement.setString(2, canExecute?:"{}")
                    preparedStatement.setString(3, data?:"{\"type\": \"dummy\", \"data\": {}}")
                    preparedStatement.executeUpdate()
                    return true
                }
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("Register error ", e)
            }
            return false
        }
        fun exists(callbackId: Long) =
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("SELECT * FROM $tableName WHERE callback_id = ?;")
                preparedStatement.setLong(1, callbackId)
                preparedStatement.executeQuery().next()
            } catch (e: SQLException) {
                ZixaMCTGBridge.logger.error("isUserRegistered error", e)
                false
            }

        private fun resolve(callbackId: Long) = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT JSON_UNQUOTE(JSON_EXTRACT(data, '$.type')) FROM $tableName WHERE callback_id = ?;")
            preparedStatement.setLong(1, callbackId)
            val query = preparedStatement.executeQuery()
            query.next()
            query.getString(1)
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("isUserRegistered error", e)
            null
        }
        private fun getRandom(): Long {
            var gen: Long? = null
            while (gen == null || exists(gen)) {
                gen = random.nextLong(999999999999)
            }
            return gen
        }
        class Builder<T: CallbackData> internal constructor(
            var display: String,
            var type: String,
            var data: T,
            var canExecute: CallbackCanExecute = CallbackCanExecute(),
        ) {
            fun pull(): Long {
                val callbackId = getRandom()
                create(callbackId, gson.toJson(canExecute), gson.toJson(TgCallback(type, data)))
                return callbackId
            }
            fun with(mod: (T) -> T): Builder<T> =
                Builder(display, type, mod(data))
            fun inline() =
                TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = display,
                    callback_data = pull().toString()
                )
            fun inlineAndId() =
                pull().let {
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                        text = display,
                        callback_data = it.toString()
                    ) to it
                }
        }
    }

    val callback
        get() = TgCallback(type, data)
    fun drop() {
        try {
            reConnect()
            if (exists(callbackId)) {
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("DELETE FROM $tableName WHERE callback_id = ?;")
                preparedStatement.setLong(1, callbackId)
                preparedStatement.executeUpdate()
            }
        } catch (e: SQLException) {
            ZixaMCTGBridge.logger.error("Register error ", e)
        }
    }
}