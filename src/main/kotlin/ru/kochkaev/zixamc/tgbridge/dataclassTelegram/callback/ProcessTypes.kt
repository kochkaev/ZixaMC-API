package ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback

import ru.kochkaev.zixamc.tgbridge.ServerBot.config

object ProcessTypes {
    val GROUP_WAITING_NAME = ProcessType(GroupWaitingNameProcessData::class.java, "GROUP_WAITING_NAME")
    val GROUP_SELECT_TOPIC_FEATURE = ProcessType(GroupSelectTopicProcessData::class.java, "GROUP_SELECT_TOPIC_FEATURE")
    val GROUP_CHATSYNC_WAITING_PREFIX = ProcessType(GroupChatSyncWaitPrefixProcessData::class.java, "GROUP_CHATSYNC_WAITING_PREFIX")
    val DUMMY = ProcessType(DummyProcessData::class.java, "DUMMY")

    val entries = hashMapOf<String, ProcessType<*>>(
        GROUP_WAITING_NAME.serializedName to GROUP_WAITING_NAME,
        GROUP_SELECT_TOPIC_FEATURE.serializedName to GROUP_SELECT_TOPIC_FEATURE,
        GROUP_CHATSYNC_WAITING_PREFIX.serializedName to GROUP_CHATSYNC_WAITING_PREFIX,
        DUMMY.serializedName to DUMMY,
    )
    fun registerType(type: ProcessType<*>) {
        entries[type.serializedName] = type
    }
}
