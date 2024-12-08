package ru.kochkaev.zixamc.tgbridge.chatSync

import net.kyori.adventure.text.Component

data class TBPlayerEventData(
    val username: String,
    val text: Component,
)
