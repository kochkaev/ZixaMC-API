package ru.kochkaev.zixamc.consoleintegration

import ru.kochkaev.zixamc.api.formatLang
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.telegram.ServerBot.bot
import ru.kochkaev.zixamc.api.telegram.ServerBot.config
import ru.kochkaev.zixamc.api.sql.feature.TopicFeatureType
import ru.kochkaev.zixamc.api.sql.feature.data.TopicFeatureData

object ConsoleFeatureType: TopicFeatureType<TopicFeatureData>(
    model = TopicFeatureData::class.java,
    serializedName = "CONSOLE",
    tgDisplayName = { config.group.features.console.display },
    tgDescription = { config.group.features.console.description },
    tgOnDone = {
        if (bot.getChat(it.chatId).isForum)
            config.group.features.console.doneTopic
        else config.group.features.console.doneNoTopic
    },
    checkAvailable = { it.hasProtectedLevel(AccountType.ADMIN) },
    getDefault = { TopicFeatureData() },
    optionsResolver = {
        config.group.features.console.options.formatLang(
            "topicId" to (it.topicId?.toString() ?: config.group.settings.nullTopicPlaceholder),
        )
    },
)