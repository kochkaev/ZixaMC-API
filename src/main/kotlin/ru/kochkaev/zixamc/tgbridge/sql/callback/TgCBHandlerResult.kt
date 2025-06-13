package ru.kochkaev.zixamc.tgbridge.sql.callback

data class TgCBHandlerResult(
    val deleteMarkup: Boolean = false,
    val deleteAllLinked: Boolean = deleteMarkup,
    val deleteCallback: Boolean = deleteAllLinked,
) {
    companion object {
        /** Erase only current SQLCallback */
        val DELETE_CALLBACK = TgCBHandlerResult(
            deleteCallback = true
        )
        /** Erase current and linked SQLCallback (that in one reply markup) and reply markup (bot.editMessageReplyMarkup) */
        val DELETE_MARKUP = TgCBHandlerResult(
            deleteMarkup = true
        )
        /** Erase current and linked SQLCallback (that in one reply markup), do not erase reply markup */
        val DELETE_LINKED = TgCBHandlerResult(
            deleteAllLinked = true
        )
        /** Do nothing */
        val SUCCESS = TgCBHandlerResult()
    }
}