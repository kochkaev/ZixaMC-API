package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup

abstract class LinkedEntity<T, V>(val key: V, private val getter: (V) -> T?) {
    fun getSQL() =
        getter.invoke(key)
    fun getSQLAssert() =
        getter.invoke(key)!!
}
class LinkedUser(key: Long): LinkedEntity<SQLEntity, Long> (key, { SQLEntity.get(it) })
class LinkedGroup(key: Long): LinkedEntity<SQLGroup, Long> (key, { SQLGroup.get(it) })