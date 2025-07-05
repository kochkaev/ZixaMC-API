package ru.kochkaev.zixamc.chatsync

data class TBCommandContext(
    val reply: (text: String) -> Unit
)
