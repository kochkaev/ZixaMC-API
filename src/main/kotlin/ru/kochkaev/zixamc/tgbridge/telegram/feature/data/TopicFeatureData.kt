package ru.kochkaev.zixamc.tgbridge.telegram.feature.data

import ru.kochkaev.zixamc.api.sql.SQLGroup

open class TopicFeatureData(
    open var topicId: Int? = null,
    group: SQLGroup? = null,
) : FeatureData(group)