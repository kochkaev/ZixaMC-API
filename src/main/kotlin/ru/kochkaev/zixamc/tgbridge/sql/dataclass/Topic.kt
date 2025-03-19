package ru.kochkaev.zixamc.tgbridge.sql.dataclass

data class Topic<R: TopicData>(
    val model: Class<R>,
    val serializedName: String,
    val tgDisplayName: () -> String = { serializedName },
    val tgDescription: () -> String = { "" }
)
