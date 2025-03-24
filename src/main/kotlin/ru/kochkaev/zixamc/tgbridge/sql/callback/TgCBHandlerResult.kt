package ru.kochkaev.zixamc.tgbridge.sql.callback

data class TgCBHandlerResult(
    val deleteMarkup: Boolean = false,
    val deleteAllLinked: Boolean = deleteMarkup,
    val deleteCallback: Boolean = deleteAllLinked,
) {
    companion object {
        val DELETE_CALLBACK = TgCBHandlerResult(
            deleteCallback = true
        )
        val DELETE_MARKUP = TgCBHandlerResult(
            deleteMarkup = true
        )
        val DELETE_LINKED = TgCBHandlerResult(
            deleteAllLinked = true
        )
        val SUCCESS = TgCBHandlerResult()
    }
}