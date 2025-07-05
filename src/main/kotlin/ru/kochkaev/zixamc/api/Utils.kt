package ru.kochkaev.zixamc.api

fun String.formatLang(vararg args: Pair<String, String>): String {
    args.forEach {
        replace("{${it.first}}", it.second)
    }
    return this
}
fun String.escapeHTML(): String = this
    .replace("&", "&amp;")
    .replace(">", "&gt;")
    .replace("<", "&lt;")