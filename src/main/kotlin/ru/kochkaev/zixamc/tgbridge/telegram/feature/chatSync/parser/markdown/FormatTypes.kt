package ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.markdown

import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.markdown.FormatType.Companion.isAllInOneRegular
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.markdown.FormatType.Companion.isAllInRegular
import ru.kochkaev.zixamc.tgbridge.telegram.feature.chatSync.parser.markdown.FormatType.Companion.splitNodes


enum class FormatTypes : FormatType {
    REGULAR {
        override val processor: ProcessFormat? = null
    },
    PRE {
//        override val regex = Regex("((?<!\\\\)```(.*?)(?<!\\\\)```)|((?!((?<!\\\\)```(.*?)(?<!\\\\)```)).)+")
        override val key = "```"
        override val tag = "pre"
        override val allowSubNode: Boolean = false
    },
    CODE {
//        override val regex = Regex("((?<!\\\\)`(.*?)(?<!\\\\)`)|((?!((?<!\\\\)`(.*?)(?<!\\\\)`)).)+")
        override val key = "`"
        override val tag = "code"
        override val allowSubNode: Boolean = false
    },
    LINK {
        override val regex = Regex("((?<!\\\\)\\[(.+?)(?<!\\\\)]\\((\\S+?)(?<!\\\\)\\))|((?!((?<!\\\\)\\[(.*?)(?<!\\\\)]\\((.*?)(?<!\\\\)\\))).)+")
        val displayRegex = Regex("(?<!\\\\)\\[(.+?)(?<!\\\\)]\\(")
        val linkRegex = Regex("(?<!\\\\)]\\((\\S+?)(?<!\\\\)\\)")
        override val processor: ProcessFormat = ProcessFormat {
            var success = false
            val parsed: ArrayList<Node> = arrayListOf()
            val content = it.build()
            var i = 0
            val haveSubs = it.subNode.isNotEmpty()
            for (result in regex.findAll(content)) {
                val value = result.value
                val len = value.length
                if (value.startsWith("[")) {
                    val sn: ArrayList<Node> = if (haveSubs) it.subNode else arrayListOf(it)
                    val allInRegular = isAllInRegular(sn, i, len)
                    val allInOneRegular = isAllInOneRegular(sn, i, len)
                    val displayMath = displayRegex.find(value)
                    val urlMath = linkRegex.find(value)
                    var url: String? = null
                    val allow = displayMath != null && urlMath != null
                    if (allow) {
                        url = urlMath!!.value
                        url = url.substring(2,url.length-1)
                    }
                    if (allow && allInOneRegular) {
                        if (!success) it.subNode.addAll(parsed)
                        var display = displayMath!!.value
                        display = display.substring(1,display.length-2)
                        it.subNode.add(LinkNode(display, url!!))
                        success = true
                    }
                    else if (allow && allInRegular && haveSubs) {
                        splitNodes(it, i, i+len, url!!)
                        i += 5 + 2*(tag.length-key.length)
                        success = true
                    }
                } else (if (!success) parsed else it.subNode).add(Node.of(result.value, REGULAR))
                i += len
            }
            return@ProcessFormat success
        }
        private fun splitNodes(node: RegularNode, start: Int, end: Int, url: String) {
            val list: ArrayList<Node> = arrayListOf()
            var tempNode: Node? = null
            var i = 0
            for (it in node.subNode) {
                val content = it.build()
                val len = content.length
                if (it.format == REGULAR && start in i..<i+len) {
                    val j = start-i
                    list.add(RegularNode(content.substring(0, j)))
                    tempNode = LinkNode(content.substring(j+1), url)
                    list.add(tempNode)
                } else if (it.format == REGULAR && end in i..i+len) {
                    val j = end-i
                    tempNode!!.subNode.add(RegularNode(content.substring(0, j-url.length-3)))
//                    list.add(RegularNode(content.substring(j)))
                } else if (i !in start..<end) list.add(it)
                else tempNode?.subNode?.add(it)
                i+=len
            }
            node.subNode.clear()
            node.subNode.addAll(list)
        }
    },
//    BOLD_ITALIC {
//        override val key = "***"
//        override val processor = ProcessFormat {
//            var success = false
//            val parsed: ArrayList<Node> = arrayListOf()
//            val content = it.build()
//            var i = 0
//            val haveSubs = it.subNode.isNotEmpty()
//            for (result in regex.findAll(content)) {
//                val value = result.value
//                val len = value.length
//                if (value.startsWith(key) && value != key) {
//                    val ifItalic = RegularNode(value)
//                    ITALIC.processor!!.parse(ifItalic)
//                    val ifBold = RegularNode(value)
//                    BOLD.processor!!.parse(ifBold)
//                    if (ifItalic.subNode[0].build().length<=ifBold.subNode[0].build().length){
//                        if (!success) it.subNode.addAll(parsed)
//                        it.subNode.add(ifBold)
//                        success = true
//                    } else {
//                        if (!success) it.subNode.addAll(parsed)
//                        it.subNode.add(ifItalic)
//                        success = true
//                    }
//                } else (if (!success) parsed else it.subNode).add(Node.of(result.value, REGULAR))
//                i += len
//            }
//            return@ProcessFormat success
//        }
//    },
    BOLD {
//        override val regex = Regex("((?<!\\\\)\\*\\*(.*?)(?<!\\\\)\\*\\*)|((?!((?<!\\\\)\\*\\*(.*?)(?<!\\\\)\\*\\*)).)+")
        override val key = "**"
        override val tag = "b"
    },
    ITALIC {
//        override val regex = Regex("((?<!\\\\)\\*(.*?)(?<!\\\\)\\*)|((?!((?<!\\\\)\\*(.*?)(?<!\\\\)\\*)).)+")
        override val key = "*"
        override val tag = "i"
    },
    UNDERLINE {
//        override val regex = Regex("((?<!\\\\)__(.*?)(?<!\\\\)__)|((?!((?<!\\\\)__(.*?)(?<!\\\\)__)).)+")
        override val key = "__"
        override val tag = "u"
    },
    STRIKETHROUGH {
//        override val regex = Regex("((?<!\\\\)~~(.*?)(?<!\\\\)~~)|((?!((?<!\\\\)~~(.*?)(?<!\\\\)~~)).)+")
        override val key = "~~"
        override val tag = "s"
    },
    SPOILER {
//        override val regex = Regex("((?<!\\\\)\\|\\|(.*?)(?<!\\\\)\\|\\|)|((?!((?<!\\\\)\\|\\|(.*?)(?<!\\\\)\\|\\|)).)+")
        override val key = "||"
        override val tag = "tg-spoiler"
    };

    companion object {
        val formats: ArrayList<FormatType> = arrayListOf()

        init {
            formats.addAll(entries)
        }
        fun register(format: FormatType) {
            formats.add(format)
        }
    }
}