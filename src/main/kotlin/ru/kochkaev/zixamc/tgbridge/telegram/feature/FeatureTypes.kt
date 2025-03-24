package ru.kochkaev.zixamc.tgbridge.telegram.feature

import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.sql.data.AccountType
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.FeatureData
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.TopicFeatureData
import ru.kochkaev.zixamc.tgbridge.telegram.feature.type.ChatSyncFeatureType

object FeatureTypes {
    val CHAT_SYNC = ChatSyncFeatureType
    val CONSOLE = TopicFeatureType(
        model = TopicFeatureData::class.java,
        serializedName = "CONSOLE",
        tgDisplayName = { config.integration.group.features.console.display },
        tgDescription = { config.integration.group.features.console.description },
        tgOnDone = {
            if (bot.getChat(it.chatId).isForum)
                config.integration.group.features.console.doneTopic
            else config.integration.group.features.console.doneNoTopic
        },
        checkAvailable = { it.hasProtectedLevel(AccountType.ADMIN) },
        getDefault = { TopicFeatureData() },
        optionsResolver = {
            TextParser.formatLang(
                text = config.integration.group.features.console.options,
                "topicId" to (it.topicId?.toString() ?: config.integration.group.settings.nullTopicPlaceholder),
            )
        },
    )

    val entries = hashMapOf<String, FeatureType<out FeatureData>>(
        CHAT_SYNC.serializedName to CHAT_SYNC,
        CONSOLE.serializedName to CONSOLE,
    )
    fun registerType(type: FeatureType<*>) {
        entries[type.serializedName] = type
    }
}
