package ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync

import net.kyori.adventure.text.Component
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgEntity
import java.time.Instant

enum class LastMessageType {
    TEXT,
    LEAVE,
}

data class LastMessage(
    val type: LastMessageType,
    val id: Int,
    var date: Instant,
    var text: String? = null,
    var entities: List<TgEntity>? = null,
    val leftPlayer: String? = null,
    var componentOfLastAppend: Component? = null
)