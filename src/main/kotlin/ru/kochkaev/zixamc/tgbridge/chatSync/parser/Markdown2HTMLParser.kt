package ru.kochkaev.zixamc.tgbridge.chatSync.parser

import ru.kochkaev.zixamc.tgbridge.MySQLIntegration
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.markdown.RegularNode
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
        val node = RegularNode(markdown)
        val parsed = node.parse().build()
        return parsed
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
        textLinkAllTextRegex: Regex = this.textLinkAllTextRegex,
        textLinkRegex: Regex = this.textLinkRegex,
        textLinkTextRegex: Regex = this.textLinkTextRegex,
        textLinkURLRegex: Regex = this.textLinkURLRegex,
    ): String {
        val codeOrPreBlocksMap = mutableMapOf<Int, Int>()
        val codeOrPreList = listOf("```", "`")
        var codeOrPreBlockOpenedAt = 0

        val stack = Stack<String>()
        val stackText = Stack<StringBuilder>()
//        val result = StringBuilder()
        stackText.add(StringBuilder())
        var i = 0

        while (i < markdown.length) {
            when {
                // Обработка экранирования
                markdown[i] == '\\' && i + 1 < markdown.length -> {
                    stackText.last().append(markdown[i + 1])
                    i += 2
                }
                // Обработка ключей
                else -> {
                    var matched = false
                    for ((key, tag) in tagMap.entries.sortedByDescending { it.key.length }) {
                        if (markdown.startsWith(key, i)) {
                            if (stack.isNotEmpty() && codeOrPreList.contains(stack.last()) && stack.last() != key) stackText.last().append(key)
                            else if (stack.isNotEmpty() && stack.contains(key)) {
                                var stackKey = stack.pop()
                                while (stackKey != key) {
                                    val stackTag = tagMap[key] ?: ""
                                    val text = stackText.pop()
                                    text.replace(text.indexOf("<$stackTag>"), tag.length+2, key)
                                    stackText.last().append(text)
                                    stackKey = stack.pop()
                                }
                                // Закрытие тега
                                val text = stackText.pop()
                                text.append("</${tag}>")
                                stackText.last().append(text)
                                stack.removeAt(stack.lastIndex)
                                if (codeOrPreList.contains(key)) {
                                    stack.forEach {
                                        val new = StringBuilder("<${tagMap[it]}>")
                                        stackText.add(new)
                                    }
                                    codeOrPreBlocksMap[codeOrPreBlockOpenedAt] = getSumLen(stackText)
                                }
                            } else if (stack.isEmpty() || (stack.last() != key && !stack.contains(key))) {
                                // Открытие тега
                                if (codeOrPreList.contains(key)) {
                                    var block = StringBuilder()
                                    stack.asReversed().forEach {
                                        val text = stackText.pop()
                                        text.append("</${tagMap[it]}>")
                                        block = text.append(block)
                                    }
                                    stackText.add(block)
                                    codeOrPreBlockOpenedAt = getSumLen(stackText)
                                }
                                val text = StringBuilder("<${tag}>")
                                stackText.add(text)
                                stack.add(key)
                            } else {
                                // Игнорируем некорректное вложение
                                stackText.last().append(tag)
                            }
                            i += key.length
                            matched = true
                            break
                        }
                    }

                    if (!matched) {
                        stackText.last().append(markdown[i])
                        i++
                    }
                }
            }
        }

        // Закрываем все незакрытые теги
        while (stack.isNotEmpty()) {
            val key = stack.removeAt(stack.lastIndex)
            val tag = tagMap[key] ?: ""
            val text = stackText.pop()
//            text.replace(Regex("(<$tag>).*?"), key)
            text.replace(text.indexOf("<$tag>"), tag.length+2, key)
            stackText.last().append(text)
//            result.append("</${tag}>")
            if (codeOrPreList.contains(key)) stack.clear()
        }

        var parsed = stackText.pop().toString()
        if (parsed.matches(textLinkAllTextRegex)) parsed =
            textLinkRegex.replace(parsed) { matched ->
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
        ZixaMCTGBridge.logger.info(parsed)
        return parsed
    }

    fun escapeMentions(text: String): String =
        text.replace(Regex("(@[A-Za-z0-9_]*+)")) {
            val mention = it.value
            val nickname = mention.replace("@", "")
            if (MySQLIntegration.isNicknameTaken(nickname))
                "<a href=\"tg://user?id=${MySQLIntegration.getLinkedEntityByNickname(nickname)?.userId}\">$mention</a>"
            else mention
        }

    private fun getSumLen(stack: Stack<StringBuilder>): Int {
        var i = 0;
        stack.forEach { i += it.length }
        return i
    }

}