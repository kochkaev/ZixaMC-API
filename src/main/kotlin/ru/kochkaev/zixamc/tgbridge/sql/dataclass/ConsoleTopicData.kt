package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import ru.kochkaev.zixamc.tgbridge.config.TextData

data class ConsoleTopicData (
    override var topicId: Int? = null,
): TopicFeatureData
