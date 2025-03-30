package ru.kochkaev.zixamc.tgbridge.telegram.feature.data

import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup

open class FeatureData(
    @Transient open var group: SQLGroup? = null
) {
//    fun setGroup(group: SQLGroup) { this.group = group }
}