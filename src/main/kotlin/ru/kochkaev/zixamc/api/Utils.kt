package ru.kochkaev.zixamc.api

fun String.formatLang(vararg args: Pair<String, String>): String {
    var formatted = this
    args.forEach {
        formatted = formatted.replace("{${it.first}}", it.second)
    }
    return formatted
}
fun String.escapeHTML(): String = this
    .replace("&", "&amp;")
    .replace(">", "&gt;")
    .replace("<", "&lt;")