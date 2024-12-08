package ru.kochkaev.zixamc.tgbridge.chatSync

import net.kyori.adventure.text.Component
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgEntity
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