package ru.kochkaev.zixamc.tgbridge.telegram.feature.data

import ru.kochkaev.zixamc.tgbridge.config.TextData

class ChatSyncTopicData (
    topicId: Int? = null,
    var enabled: Boolean = true,
//    val name: String? = null,
//    val aliases: ArrayList<String> = arrayListOf(),
    var prefix: TextData? = null,
    var fromMcPrefix: TextData? = null,
): TopicFeatureData(topicId)
