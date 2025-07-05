package ru.kochkaev.zixamc.chatsync.parser.markdown

fun interface ProcessFormat {
    fun parse(node: RegularNode):Boolean
}