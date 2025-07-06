package ru.kochkaev.zixamc.api.sql.callback

data class TgCBHandlerResult(
    val deleteMessage: Boolean = false,
    val deleteMarkup: Boolean = deleteMessage,
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
        /** Delete message and erase current and linked SQLCallback (that in one reply markup) */
        val DELETE_MESSAGE = TgCBHandlerResult(
            deleteMessage = true
        )
        /** Erase current and linked SQLCallback (that in one reply markup), do not erase reply markup */
        val DELETE_LINKED = TgCBHandlerResult(
            deleteAllLinked = true
        )
        /** Do nothing */
        val SUCCESS = TgCBHandlerResult()
    }
}