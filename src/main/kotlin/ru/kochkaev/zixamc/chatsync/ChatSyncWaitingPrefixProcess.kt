package ru.kochkaev.zixamc.chatsync

import ru.kochkaev.zixamc.chatsync.GroupChatSyncWaitPrefixProcessData
import ru.kochkaev.zixamc.api.sql.process.ProcessType
import ru.kochkaev.zixamc.api.sql.process.ProcessorType

object ChatSyncWaitingPrefixProcess: ProcessType<GroupChatSyncWaitPrefixProcessData>(
    model = GroupChatSyncWaitPrefixProcessData::class.java,
    serializedName = "GROUP_CHATSYNC_WAITING_PREFIX",
    processorType = ProcessorType.REPLY_MESSAGE,
    processor = ChatSyncFeatureType::waitPrefixProcessor,
)