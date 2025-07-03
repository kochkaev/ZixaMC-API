package ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser

import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.markdown.RegularNode
import ru.kochkaev.zixamc.tgbridge.sql.SQLUser
import java.util.*

object Markdown2HTMLParser {

    // (?<!\\) -> not "\" before key;
    private val textLinkAllTextRegex = Regex("((.*?)(?<!\\\\)\\[(.*?)(?<!\\\\)]\\((.*?)(?<!\\\\)\\))*?(.*?)")
    private val textLinkRegex = Regex("(?<!\\\\)\\[(.*?)(?<!\\\\)]\\((.*?)(?<!\\\\)\\)")
    private val textLinkTextRegex = Regex("(?<!\\\\)\\[(.*?)(?<!\\\\)]\\(")
    private val textLinkURLRegex = Regex("(?<!\\\\)]\\((.*?)(?<!\\\\)\\)")

    fun parse(
        markdown: String
    ): String {
        if (ServerBot.config.chatSync.betaMarkdown) {
            val node = RegularNode(markdown)
            val parsed = node.parse().build()
            return parsed
        } else return parseOld(markdown)
    }
    fun parseOld(
        markdown: String,
        tagMap: Map<String, String> = mapOf(
            "**" to "b",
            "*" to "i",
            "__" to "u",
            "~~" to "s",
            "||" to "tg-spoiler",
            "```" to "pre",
            "`" to "code",
        ),
        textLinkAllTextRegex: Regex = Markdown2HTMLParser.textLinkAllTextRegex,
        textLinkRegex: Regex = Markdown2HTMLParser.textLinkRegex,
        textLinkTextRegex: Regex = Markdown2HTMLParser.textLinkTextRegex,
        textLinkURLRegex: Regex = Markdown2HTMLParser.textLinkURLRegex,
    ): String {
        val codeOrPreBlocksMap = mutableMapOf<Int, Int>()
        val codeOrPreList = listOf("```", "`")
        var codeOrPreBlockOpenedAt = 0

        val stack = Stack<String>()
        val result = StringBuilder()
        var i = 0

        while (i < markdown.length) {
            when {
                // Обработка экранирования
                markdown[i] == '\\' && i + 1 < markdown.length -> {
                    result.append(markdown[i + 1])
                    i += 2
                }
                // Обработка ключей
                else -> {
                    var matched = false
                    for ((key, tag) in tagMap.entries.sortedByDescending { it.key.length }) {
                        if (markdown.startsWith(key, i)) {
                            if (stack.isNotEmpty() && codeOrPreList.contains(stack.last()) && stack.last() != key) result.append(key)
                            else if (stack.isNotEmpty() && stack.last() == key) {
                                // Закрытие тега
                                result.append("</${tag}>")
                                stack.removeAt(stack.lastIndex)
                                if (codeOrPreList.contains(key)) {
                                    stack.forEach { result.append("<${tagMap[it]}>") }
                                    codeOrPreBlocksMap[codeOrPreBlockOpenedAt] = result.length
                                }
                            } else if (stack.isEmpty() || (stack.last() != key && !stack.contains(key))) {
                                // Открытие тега
                                if (codeOrPreList.contains(key)) {
                                    stack.asReversed()
                                        .forEach { result.append("</${tagMap[it]}>") }
                                    codeOrPreBlockOpenedAt = result.length
                                }
                                result.append("<${tag}>")
                                stack.add(key)
                            } else {
                                // Игнорируем некорректное вложение
                                result.append(key)
                            }
                            i += key.length
                            matched = true
                            break
                        }
                    }

                    if (!matched) {
                        result.append(markdown[i])
                        i++
                    }
                }
            }
        }

        // Закрываем все незакрытые теги
        while (stack.isNotEmpty()) {
            val key = stack.removeAt(stack.lastIndex)
            val tag = tagMap[key] ?: ""
//            result.replace(result.lastIndexOf("<$tag>"), tag.length+2, key)
            result.append("</${tag}>")
            if (codeOrPreList.contains(key)) stack.clear()
        }

        var parsed = result.toString()
        if (parsed.matches(textLinkAllTextRegex)) parsed =
            textLinkRegex.replace(result.toString()) { matched ->
                var isInCodeOrPreBlock = false
                codeOrPreBlocksMap.forEach { (start, end) -> if (matched.range.first>start && matched.range.last<end) isInCodeOrPreBlock = true }
                if (isInCodeOrPreBlock) return@replace matched.value
                val linkMatched = textLinkURLRegex.find(matched.value) ?: return@replace matched.value
                val displayTextMatched = textLinkTextRegex.find(matched.value) ?: return@replace matched.value
                "<a href=\"${
                    linkMatched.value.substring(2, linkMatched.value.length - 1)
                }\">${
                    displayTextMatched.value.substring(1, displayTextMatched.value.length - 2)
                }</a>"
            }
        parsed = escapeMentions(parsed)
        return parsed
    }

    fun escapeMentions(text: String): String =
        text.replace(Regex("(@[A-Za-z0-9_]*+)")) {
            val mention = it.value
            val nickname = mention.replace("@", "")
            if (SQLUser.exists(nickname))
                "<a href=\"tg://user?id=${SQLUser.get(nickname)?.userId}\">$mention</a>"
            else mention
        }

    private fun getSumLen(stack: Stack<StringBuilder>): Int {
        var i = 0;
        stack.forEach { i += it.length }
        return i
    }

}