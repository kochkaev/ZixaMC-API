package ru.kochkaev.zixamc.api.sql

import com.google.gson.annotations.JsonAdapter
import io.leangen.geantyref.TypeToken
import ru.kochkaev.zixamc.api.ZixaMC
import ru.kochkaev.zixamc.api.config.GsonManager.gson
import ru.kochkaev.zixamc.api.config.serialize.SQLCallbackAdapter
import ru.kochkaev.zixamc.api.sql.callback.CallbackCanExecute
import ru.kochkaev.zixamc.api.sql.callback.CallbackData
import ru.kochkaev.zixamc.api.sql.callback.TgCallback
import ru.kochkaev.zixamc.api.sql.util.AbstractSQLField
import ru.kochkaev.zixamc.api.sql.util.CallbacksSQLArray
import ru.kochkaev.zixamc.api.sql.util.LongSQLField
import ru.kochkaev.zixamc.api.sql.util.NullableIntSQLField
import ru.kochkaev.zixamc.api.telegram.model.ITgMenuButton
import ru.kochkaev.zixamc.api.telegram.model.TgInlineKeyboardMarkup
import java.sql.SQLException
import java.util.Random
import kotlin.collections.get

@JsonAdapter(SQLCallbackAdapter::class)
class SQLCallback<T: CallbackData> private constructor(
    val callbackId: Long,
) {
    val linked = CallbacksSQLArray(SQLCallback, "linked", callbackId, "callback_id")
    private val chatIdField = LongSQLField(SQLCallback, "chat_id", callbackId, "callback_id")
    var chatId: Long
        get() = chatIdField.get() ?: 0
        set(chatId) { chatIdField.set(chatId) }
    private val messageIdField = NullableIntSQLField(SQLCallback, "message_id", callbackId, "callback_id")
    var messageId: Int?
        get() = messageIdField.get()
        set(messageId) { messageIdField.set(messageId) }
    private val canExecuteField = AbstractSQLField<CallbackCanExecute>(SQLCallback, "can_execute", callbackId, "callback_id", CallbackCanExecute::class.java)
    var canExecute: CallbackCanExecute?
        get() = canExecuteField.get()
        set(canExecute) { canExecuteField.set(canExecute?:CallbackCanExecute()) }
    private val dataField = AbstractSQLField(SQLCallback, "data", callbackId, "callback_id",
        type = null,
        getter = { rs -> gson.fromJson<TgCallback<T>>(rs.getString(1), TgCallback::class.java).data },
        setter = { ps, it -> ps.setString(1, gson.toJson(TgCallback(type, it)))},
    )
    var data: T?
        get() = dataField.get()
        set(data) { dataField.set(data) }
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
                    `chat_id` BIGINT NOT NULL,
                    `message_id` INTEGER DEFAULT NULL,
                    `linked` JSON NOT NULL DEFAULT "[]",
                    `can_execute` JSON NOT NULL DEFAULT "{}",
                    `data` JSON NOT NULL DEFAULT "{}",
                    PRIMARY KEY (`id`), UNIQUE (`callback_id`)
                ) ENGINE = InnoDB;
                """.trimIndent(),
                config.database,
                tableName
            )
        private val random = Random()

        fun get(callbackId: Long) =
            if (exists(callbackId)) getWithoutCheck(callbackId)
            else null
        @Deprecated("Not safe", replaceWith = ReplaceWith("get(callbackId)"))
        fun getWithoutCheck(callbackId: Long) =
            registries[resolve(callbackId)]?.let { get(callbackId, it) }
        private fun <T: CallbackData> get(callbackId: Long, model: Class<T>) =
            SQLCallback<T>(callbackId)
        fun getAll(chatId: Long, messageId: Int) = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT callback_id FROM $tableName WHERE chat_id = ? AND message_id = ?;")
            preparedStatement.setLong(1, chatId)
            preparedStatement.setInt(2, messageId)
            val query = preparedStatement.executeQuery()
            val callbacks = arrayListOf<SQLCallback<out CallbackData>>()
            while (query.next())
                get(query.getLong(1))?.also { callbacks.add(it) }
            callbacks
        } catch (e: SQLException) {
            ZixaMC.logger.error("Register error ", e)
            listOf()
        }
        fun getAll(chatId: Long) = try {
            reConnect()
            val preparedStatement =
                MySQLConnection!!.prepareStatement("SELECT callback_id FROM $tableName WHERE chat_id = ?;")
            preparedStatement.setLong(1, chatId)
            val query = preparedStatement.executeQuery()
            val callbacks = arrayListOf<SQLCallback<out CallbackData>>()
            while (query.next())
                get(query.getLong(1))?.also { callbacks.add(it) }
            callbacks
        } catch (e: SQLException) {
            ZixaMC.logger.error("Register error ", e)
            listOf()
        }
        fun dropAll(chatId: Long, messageId: Int) {
            getAll(chatId, messageId).forEach { it.drop() }
        }
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
        fun create(callbackId: Long, chatId: Long, canExecute: String?, data:String?): Boolean {
            try {
                reConnect()
                if (!exists(callbackId)) {
                    val preparedStatement =
                        MySQLConnection!!.prepareStatement("INSERT INTO $tableName (callback_id, chat_id, can_execute, data) VALUES (?, ?, ?, ?);")
                    preparedStatement.setLong(1, callbackId)
                    preparedStatement.setLong(2, chatId)
                    preparedStatement.setString(3, canExecute?:"{}")
                    preparedStatement.setString(4, data?:"{\"type\": \"dummy\", \"data\": {}}")
                    preparedStatement.executeUpdate()
                    return true
                }
            } catch (e: SQLException) {
                ZixaMC.logger.error("Register error ", e)
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
                ZixaMC.logger.error("isUserRegistered error", e)
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
            ZixaMC.logger.error("isUserRegistered error", e)
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
        ): ITgMenuButton {
            fun pull(chatId: Long): Long {
                val callbackId = getRandom()
                create(callbackId, chatId, gson.toJson(canExecute), gson.toJson(TgCallback(type, data)))
                return callbackId
            }
            fun with(mod: (T) -> T): Builder<T> =
                Builder(display, type, mod(data))
            fun inline(chatId: Long) =
                TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                    text = display,
                    callback_data = pull(chatId).toString()
                )
            fun inlineAndId(chatId: Long) =
                pull(chatId).let {
                    TgInlineKeyboardMarkup.TgInlineKeyboardButton(
                        text = display,
                        callback_data = it.toString()
                    ) to it
                }
        }
        fun migrateChatId(oldChatId: Long, newChatId: Long) {
            try {
                reConnect()
                val preparedStatement =
                    MySQLConnection!!.prepareStatement("UPDATE ${SQLProcess.tableName} SET chat_id = ? WHERE chat_id = ?;")
                preparedStatement.setLong(1, newChatId)
                preparedStatement.setLong(2, oldChatId)
                preparedStatement.executeUpdate()
            } catch (e: SQLException) {
                ZixaMC.logger.error("Register error ", e)
            }
        }
    }

    val callback
        get() = TgCallback(type, data)
    fun drop() {
        try {
            MySQL.reConnect()
            if (exists(callbackId)) {
                val preparedStatement =
                    MySQL.MySQLConnection!!.prepareStatement("DELETE FROM $tableName WHERE callback_id = ?;")
                preparedStatement.setLong(1, callbackId)
                preparedStatement.executeUpdate()
            }
        } catch (e: SQLException) {
            ZixaMC.logger.error("Register error ", e)
        }
    }
}