package ru.kochkaev.zixamc.tgbridge.chatSync

object Markdown2HTMLParser {
    // (?<!\\) -> not "\" before key;
    private val boldRegex = Regex("(?<!\\\\)\\*\\*(.*?)(?<!\\\\)\\*\\*")
    private val italicRegex = Regex("(?<!\\\\)\\*(.*?)(?<!\\\\)\\*")
    private val underlineRegex = Regex("(?<!\\\\)__(.*?)(?<!\\\\)__")
    private val strikethroughRegex = Regex("(?<!\\\\)~~(.*?)(?<!\\\\)~~")
    private val spoilerRegex = Regex("(?<!\\\\)\\|\\|(.*?)(?<!\\\\)\\|\\|")
    private val codeRegex = Regex("(?<!\\\\)`(.*?)(?<!\\\\)`")
    private val preRegex = Regex("(?<!\\\\)```(.*?)(?<!\\\\)```")
    private val textLinkRegex = Regex("(?<!\\\\)\\[(.*?)(?<!\\\\)]\\((.*?)(?<!\\\\)\\)")
    private val textLinkTextRegex = Regex("(?<!\\\\)\\[(.*?)(?<!\\\\)]\\(")
    private val textLinkURLRegex = Regex("(?<!\\\\)]\\((.*?)(?<!\\\\)\\)")

    fun parse(text: String): String = text
        .apply {
            textLinkRegex.replace(this) { matched ->
                "<a href=${
                    textLinkURLRegex.find(matched.value)?.value?.substring(2, matched.value.length - 1)
                }>${
                    textLinkTextRegex.find(matched.value)?.value?.substring(1, matched.value.length - 2)
                }</a>"
            }
        }
        .apply {
            boldRegex.replace(this) { matched -> "<b>${matched.value.substring(2, matched.value.length - 2)}</b>" }
        }
        .apply {
            italicRegex.replace(this) { matched -> "<i>${matched.value.substring(1, matched.value.length - 1)}</i>" }
        }
        .apply {
            underlineRegex.replace(this) { matched -> "<u>${matched.value.substring(2, matched.value.length - 2)}</u>" }
        }
        .apply {
            strikethroughRegex.replace(this) { matched -> "<s>${matched.value.substring(2, matched.value.length - 2)}</s>" }
        }
        .apply {
            preRegex.replace(this) { matched -> "<pre>${matched.value.substring(1, matched.value.length - 1)}</pre>" }
        }
        .apply {
            codeRegex.replace(this) { matched -> "<code>${matched.value.substring(1, matched.value.length - 1)}</code>" }
        }
        .apply {
            spoilerRegex.replace(this) { matched -> "<tg-spoiler>${matched.value.substring(2, matched.value.length - 2)}</tg-spoiler>" }
        }
}