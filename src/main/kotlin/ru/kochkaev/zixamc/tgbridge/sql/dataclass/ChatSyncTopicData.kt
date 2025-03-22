package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import ru.kochkaev.zixamc.tgbridge.config.TextData

data class ChatSyncTopicData (
    override var topicId: Int? = null,
    var enabled: Boolean = true,
//    val name: String? = null,
//    val aliases: ArrayList<String> = arrayListOf(),
    var prefix: TextData? = null,
    var fromMcPrefix: TextData? = null,
): TopicFeatureData
