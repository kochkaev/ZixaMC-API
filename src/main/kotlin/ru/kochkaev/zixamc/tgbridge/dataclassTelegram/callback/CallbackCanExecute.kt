package ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback

import ru.kochkaev.zixamc.tgbridge.ServerBot
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgChatMemberStatuses

data class CallbackCanExecute(
    val statuses: List<TgChatMemberStatuses> =
        listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR, TgChatMemberStatuses.MEMBER, TgChatMemberStatuses.RESTRICTED),
    val display: String = ServerBot.config.integration.group.memberStatus.members,
)
