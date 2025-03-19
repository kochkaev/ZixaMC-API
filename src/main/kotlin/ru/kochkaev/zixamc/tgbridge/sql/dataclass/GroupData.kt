package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import ru.kochkaev.zixamc.tgbridge.config.TextData
import java.util.*

data class GroupData(
    val prefix: TextData? = null,
    val fromMcPrefix: TextData? = null,
    val enabledChatSync: Boolean = true,
)
