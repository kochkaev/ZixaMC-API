package ru.kochkaev.zixamc.chatsync

import net.kyori.adventure.text.Component

data class TBPlayerEventData(
    val username: String,
    val text: Component,
)
