package ru.kochkaev.zixamc.tgbridge.sql.callback

import ru.kochkaev.zixamc.tgbridge.sql.util.LinkedUser
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMemberStatuses

data class CallbackCanExecute(
    val statuses: List<TgChatMemberStatuses>? =
        listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR, TgChatMemberStatuses.MEMBER, TgChatMemberStatuses.RESTRICTED),
    val display: String = ServerBot.config.integration.group.memberStatus.members,
    val users: List<Long>? = null,
)
