package ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync

data class TBCommandContext(
    val reply: (text: String) -> Unit
)
