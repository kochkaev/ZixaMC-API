package ru.kochkaev.zixamc.tgbridge.sql.callback

import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback

class TgMenu(
    val buttons: List<List<SQLCallback.Companion.Builder<out CallbackData>>>
): ru.kochkaev.zixamc.tgbridge.telegram.model.ITgMenu {
    fun inline(chatId: Long): ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup {
        val linked = arrayListOf<SQLCallback<out CallbackData>>()
        val markup = ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup(
            buttons.map { list ->
                list
                    .map { it.inlineAndId(chatId).also { ret -> linked.add(SQLCallback.get(ret.second)!!) }.first }
            }
        )
        if (linked.size>1) linked.forEach {
            it.linked.addAllSQL(linked.filter { it1 -> it1 != it })
        }
        return markup
    }
    fun inlineAndId(chatId: Long): Pair<ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup, List<Long>> {
        val linked = arrayListOf<SQLCallback<out CallbackData>>()
        val markup = ru.kochkaev.zixamc.tgbridge.telegram.model.TgInlineKeyboardMarkup(
            buttons.map { list ->
                list
                    .map { it.inlineAndId(chatId).also { ret -> linked.add(SQLCallback.get(ret.second)!!) }.first }
            }
        )
        if (linked.size>1) linked.forEach {
            it.linked.addAllSQL(linked.filter { it1 -> it1 != it })
        }
        return markup to linked.map { it.callbackId }
    }
}
