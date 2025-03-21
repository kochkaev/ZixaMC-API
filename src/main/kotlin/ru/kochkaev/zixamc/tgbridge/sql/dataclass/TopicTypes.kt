package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import ru.kochkaev.zixamc.tgbridge.ServerBot.config

object TopicTypes {
    val CHAT_SYNC = Topic(
        model = ChatSyncTopicData::class.java,
        serializedName = "CHAT_SYNC",
        tgDisplayName = { config.integration.group.features.chatSyncDisplay },
        tgDescription = { config.integration.group.features.chatSyncDescription },
    )

    val entries = hashMapOf<String, Topic<*>>(CHAT_SYNC.serializedName to CHAT_SYNC)
    fun registerType(type: Topic<*>) {
        entries[type.serializedName] = type
    }
}
