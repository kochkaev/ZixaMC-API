package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import ru.kochkaev.zixamc.tgbridge.ServerBot.config

object FeatureTypes {
    val CHAT_SYNC = FeatureType(
        model = ChatSyncTopicData::class.java,
        serializedName = "CHAT_SYNC",
        tgDisplayName = { config.integration.group.features.chatSync.display },
        tgDescription = { config.integration.group.features.chatSync.description },
        checkAvailable = { true },
        getDefault = { ChatSyncTopicData(it) }
    )
    val CONSOLE = FeatureType(
        model = ChatSyncTopicData::class.java,
        serializedName = "CONSOLE",
        tgDisplayName = { config.integration.group.features.chatSync.display },
        tgDescription = { config.integration.group.features.chatSync.description },
        checkAvailable = { group ->
            group.getNoBotsMembers()
                .map { it.getSQL() }
                .fold(true) { acc, sql ->
                    acc && sql?.accountType == AccountType.ADMIN
                }
        },
    )

    val entries = hashMapOf<String, FeatureType<*>>(
        CHAT_SYNC.serializedName to CHAT_SYNC,
        CONSOLE.serializedName to CONSOLE,
    )
    fun registerType(type: FeatureType<*>) {
        entries[type.serializedName] = type
    }
}
