package ru.kochkaev.zixamc.tgbridge.chatSync.parser.markdown

fun interface ProcessFormat {
    fun parse(node:RegularNode):Boolean
}