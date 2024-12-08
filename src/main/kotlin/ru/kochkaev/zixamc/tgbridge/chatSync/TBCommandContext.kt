package ru.kochkaev.zixamc.tgbridge.chatSync

data class TBCommandContext(
    val reply: (text: String) -> Unit
)
