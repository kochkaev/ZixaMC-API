package ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback

import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup

interface IChatSyncType {
    suspend fun sendNeedPrefix(group: SQLGroup, replyTo: Int?, topicId: Int? = null, prefixType: GroupChatSyncWaitPrefixProcessData.PrefixTypes = GroupChatSyncWaitPrefixProcessData.PrefixTypes.DEFAULT)
}