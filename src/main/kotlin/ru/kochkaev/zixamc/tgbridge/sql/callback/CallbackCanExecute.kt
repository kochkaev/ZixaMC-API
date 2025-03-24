package ru.kochkaev.zixamc.tgbridge.sql.callback

import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot

data class CallbackCanExecute(
    val statuses: List<ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMemberStatuses> =
        listOf(ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMemberStatuses.CREATOR, ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMemberStatuses.ADMINISTRATOR, ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMemberStatuses.MEMBER, ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMemberStatuses.RESTRICTED),
    val display: String = ServerBot.config.integration.group.memberStatus.members,
)
