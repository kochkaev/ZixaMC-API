package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import ru.kochkaev.zixamc.tgbridge.config.TextData

data class GroupData(
    val topicId: Int? = null,
    val prefix: TextData? = null,
    val fromMcPrefix: TextData? = null,
    val members: List<Long> = listOf(),
)
