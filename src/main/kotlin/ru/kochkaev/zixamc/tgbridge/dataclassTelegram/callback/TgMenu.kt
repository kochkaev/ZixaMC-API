package ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback

import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgInlineKeyboardMarkup
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback

class TgMenu<T: CallbackData>(
    val buttons: List<List<SQLCallback.Companion.Builder<T>>>
) {
    fun inline(): TgInlineKeyboardMarkup {
        val linked = arrayListOf<SQLCallback<out CallbackData>>()
        val markup = TgInlineKeyboardMarkup(
            buttons.map { list -> list
                .map { it.inlineAndId().also { ret -> linked.add(SQLCallback.get(ret.second)!!) }.first }
            }
        )
        if (linked.size>1) linked.forEach {
            it.linked.addAllSQL(linked.filter { it1 -> it1 != it })
        }
        return markup
    }
    fun inlineAndId(): Pair<TgInlineKeyboardMarkup, List<Long>> {
        val linked = arrayListOf<SQLCallback<out CallbackData>>()
        val markup = TgInlineKeyboardMarkup(
            buttons.map { list -> list
                .map { it.inlineAndId().also { ret -> linked.add(SQLCallback.get(ret.second)!!) }.first }
            }
        )
        if (linked.size>1) linked.forEach {
            it.linked.addAllSQL(linked.filter { it1 -> it1 != it })
        }
        return markup to linked.map { it.callbackId }
    }
}
