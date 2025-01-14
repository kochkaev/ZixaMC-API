package ru.kochkaev.zixamc.tgbridge.chatSync.parser.markdown

interface Node {
    val format: FormatType
    val subNode: ArrayList<Node>

    companion object {
        fun of(content: String, format: FormatType) : Node =
            if (format == FormatTypes.REGULAR) RegularNode(content)
            else NodeImpl(content, format)
    }

    fun parse(): Node
    fun build(): String
}

data class NodeImpl (
    override val format: FormatType,
    override val subNode: ArrayList<Node> = arrayListOf()
) : Node {
    constructor(content: String, format: FormatType) : this(format, arrayListOf(RegularNode(content)))

    override fun parse(): NodeImpl {
        if (!format.allowSubNode) return this
        subNode.forEach { it.parse() }
        return this
    }
    override fun build(): String = subNode.joinToString("") { it.build() }
}
data class LinkNode (
    override val format: FormatType,
    val url: String,
    override val subNode: ArrayList<Node> = arrayListOf()
) : Node {
    constructor(display: String, url: String) : this(FormatTypes.LINK, url, arrayListOf(RegularNode(display)))

    override fun parse(): LinkNode {
        subNode.forEach { it.parse() }
        return this
    }
    override fun build(): String = "<a href=\"$url\">${subNode.joinToString("") { it.build() }}</a>"
}

data class RegularNode (
    val content: String,
    override val subNode: ArrayList<Node> = arrayListOf()
) : Node {
    override val format: FormatType = FormatTypes.REGULAR

    override fun parse(): RegularNode {
        if (!format.allowSubNode) return this
        var success = false
        FormatTypes.formats.forEach { success = it.processor?.parse(this) == true || success }
        if (success) subNode.forEach { it.parse() }
        return this
    }
    override fun build(): String =
        if (subNode.isNotEmpty()) subNode.joinToString("") { it.build() }
        else content
}