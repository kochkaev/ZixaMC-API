package ru.kochkaev.zixamc.tgbridge.sql.util

import com.google.gson.annotations.JsonAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.LinkedCallbackAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.LinkedGroupAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.LinkedUserAdapter
import ru.kochkaev.zixamc.tgbridge.sql.callback.CallbackData
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup

abstract class LinkedEntity<T, V>(val key: V, private val getter: (V) -> T?) {
    fun getSQL() =
        getter.invoke(key)
    fun getSQLAssert() =
        getter.invoke(key)!!
}
@JsonAdapter(LinkedUserAdapter::class)
class LinkedUser(key: Long): LinkedEntity<SQLEntity, Long> (key, { SQLEntity.get(it) })
@JsonAdapter(LinkedGroupAdapter::class)
class LinkedGroup(key: Long): LinkedEntity<SQLGroup, Long> (key, { SQLGroup.get(it) })
@JsonAdapter(LinkedCallbackAdapter::class)
class LinkedCallback(key: Long): LinkedEntity<SQLCallback<out CallbackData>, Long> (key, { SQLCallback.get(it) })