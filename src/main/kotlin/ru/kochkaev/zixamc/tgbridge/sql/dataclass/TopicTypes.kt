package ru.kochkaev.zixamc.tgbridge.sql.dataclass


object TopicTypes {
    val CHAT_SYNC = Topic(
        model = ChatSyncTopicData::class.java,
        serializedName = "CHAT_SYNC",
    )

    val entries = hashMapOf<String, Topic<*>>(CHAT_SYNC.serializedName to CHAT_SYNC)
    fun registerType(type: Topic<*>) {
        entries[type.serializedName] = type
    }
}
