package ru.kochkaev.zixamc.api.sql.callback

import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.telegram.model.ITgMenu
import ru.kochkaev.zixamc.api.telegram.model.ITgMenuButton
import ru.kochkaev.zixamc.api.telegram.model.TgInlineKeyboardMarkup

class TgMenu(
    val buttons: List<List<ITgMenuButton>>
): ITgMenu {
    fun inline(chatId: Long): TgInlineKeyboardMarkup {
        val linked = arrayListOf<SQLCallback<out CallbackData>>()
        val markup = TgInlineKeyboardMarkup(
            buttons.map { list ->
                list.mapNotNull {
                    when (it) {
                        is SQLCallback.Companion.Builder<out CallbackData> -> it.inlineAndId(chatId)
                            .also { ret -> linked.add(SQLCallback.Companion.get(ret.second)!!) }.first

                        is TgInlineKeyboardMarkup.TgInlineKeyboardButton -> it
                        else -> null
                    }
                }
            }
        )
        if (linked.size>1) linked.forEach {
            it.linked.addAllSQL(linked.filter { it1 -> it1 != it })
        }
        return markup
    }
    fun inlineAndId(chatId: Long): Pair<TgInlineKeyboardMarkup, List<Long>> {
        val linked = arrayListOf<SQLCallback<out CallbackData>>()
        val markup = TgInlineKeyboardMarkup(
            buttons.map { list ->
                list.mapNotNull {
                    when (it) {
                        is SQLCallback.Companion.Builder<out CallbackData> -> it.inlineAndId(chatId)
                            .also { ret -> linked.add(SQLCallback.Companion.get(ret.second)!!) }.first

                        is TgInlineKeyboardMarkup.TgInlineKeyboardButton -> it
                        else -> null
                    }
                }
            }
        )
        if (linked.size>1) linked.forEach {
            it.linked.addAllSQL(linked.filter { it1 -> it1 != it })
        }
        return markup to linked.map { it.callbackId }
    }
}
