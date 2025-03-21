package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.gson.annotations.JsonAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.TopicTypeAdapter

@JsonAdapter(TopicTypeAdapter::class)
data class Topic<R: TopicData>(
    val model: Class<R>,
    val serializedName: String,
    val tgDisplayName: () -> String = { serializedName },
    val tgDescription: () -> String = { "" }
)
