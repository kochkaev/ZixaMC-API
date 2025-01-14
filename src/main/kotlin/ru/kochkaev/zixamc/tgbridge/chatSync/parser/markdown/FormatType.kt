package ru.kochkaev.zixamc.tgbridge.chatSync.parser.markdown

import org.spongepowered.configurate.objectmapping.meta.NodeResolver
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.markdown.FormatTypes.REGULAR

interface FormatType {
    val regex: Regex
        get() = Regex(
            "(((?<!\\\\)${safeToRegex(key)}(.+?)(?<!\\\\)${safeToRegex(key)})(?!\\A(.)(?!${safeToRegex(key)}\\z)))|" +
                    "((?!(((?<!\\\\)${safeToRegex(key)}(.+?)(?<!\\\\)${safeToRegex(key)})(?!\\A(.)(?!${safeToRegex(key)}\\z)))).)+"
        )
    val key: String
        get() = ""
    val tag: String
        get() = ""
    val allowSubNode: Boolean
        get() = true
    val processor: ProcessFormat?
        get() = ProcessFormat {
            var success = false
            val parsed: ArrayList<Node> = arrayListOf()
            val content = it.build()
            var i = 0
            val haveSubs = it.subNode.isNotEmpty()
            for (result in regex.findAll(content)) {
                val value = result.value
                val len = value.length
                if (value.startsWith(key) && value != key) {
                    val sn: ArrayList<Node> = if (haveSubs) it.subNode else arrayListOf(it)
                    val allInRegular = isAllInRegular(sn, i, len)
                    val allInOneRegular = isAllInOneRegular(sn, i, len)
                    if (allInOneRegular) {
                        if (!success) it.subNode.addAll(parsed)
                        it.subNode.add(Node.of("<$tag>${value.substring(key.length, len - key.length)}</$tag>", this))
                        success = true
                    }
                    else if (allInRegular && haveSubs) {
                        splitNodes(it, i, i+len, this)
                        i += 5 + 2*(tag.length-key.length)
                        success = true
                    }
                } else (if (!success) parsed else it.subNode).add(Node.of(result.value, REGULAR))
                i += len
            }
            return@ProcessFormat success
        }

    companion object {
        private val safeMap = mapOf(
            "\\" to "\\\\",
            "\$" to "\\\$}",
            "*" to "\\*",
            "." to "\\.",
            "?" to "\\?",
            "|" to "\\|",
            "&" to "\\&",
            "+" to "\\+",
            "^" to "\\^",
            "(" to "\\(",
            ")" to "\\)",
            "[" to "\\[",
            "]" to "\\]",
            "{" to "\\{",
            "}" to "\\}",
        )
        fun safeToRegex(string: String): String {
            var safe = string
            for(key in safeMap.keys) {
                safe = safe.replace(key, safeMap[key]!!)
            }
            return safe
        }
        fun isAllInOneRegular(list: ArrayList<Node>, start: Int, end: Int) : Boolean {
            var i = 0
            for (node in list) {
                val content = node.build()
                val len = content.length
                if (node.format == REGULAR && (start in i..len && end in i..len)) return true
                i+=len
            }
            return false
        }
        fun isAllInRegular(list: ArrayList<Node>, start: Int, end: Int) : Boolean {
            var isAllInRegular = true
            var i = 0
            for (node in list) {
                val content = node.build()
                val len = content.length
                if (node.format != REGULAR && (start in i..<len || end in i..len)) isAllInRegular = false
                i+=len
            }
            return isAllInRegular
        }
        fun splitNodes(node: RegularNode, start: Int, end: Int, format: FormatType) {
            val list: ArrayList<Node> = arrayListOf()
            var tempNode: Node? = null
            var i = 0
            for (it in node.subNode) {
                val content = it.build()
                val len = content.length
                if (it.format == REGULAR && start in i..<i+len) {
                    val j = start-i
                    list.add(RegularNode(content.substring(0, j)))
                    tempNode = Node.of("<${format.tag}>${content.substring(j+format.key.length)}", format)
                    list.add(tempNode)
                } else if (it.format == REGULAR && end in i..i+len) {
                    val j = end-i
                    tempNode!!.subNode.add(RegularNode("${content.substring(0, j-format.key.length)}</${format.tag}>"))
//                    list.add(RegularNode(content.substring(j)))
                } else if (i !in start..<end) list.add(it)
                else tempNode?.subNode?.add(it)
                i+=len
            }
            node.subNode.clear()
            node.subNode.addAll(list)
        }
    }

}