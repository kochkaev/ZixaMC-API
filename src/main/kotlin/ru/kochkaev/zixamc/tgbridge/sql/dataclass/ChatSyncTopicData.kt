package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import ru.kochkaev.zixamc.tgbridge.config.TextData

data class ChatSyncTopicData (
    override val topicId: Int? = null,
    val enabled: Boolean = true,
//    val name: String? = null,
//    val aliases: ArrayList<String> = arrayListOf(),
    val prefix: TextData? = null,
    val fromMcPrefix: TextData? = null,
): TopicData
