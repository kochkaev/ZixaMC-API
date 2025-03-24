package ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.markdown

fun interface ProcessFormat {
    fun parse(node: RegularNode):Boolean
}