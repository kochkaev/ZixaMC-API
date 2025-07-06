package ru.kochkaev.zixamc.api.sql.process

import ru.kochkaev.zixamc.api.telegram.ServerBotGroup

object ProcessTypes {
    val GROUP_WAITING_NAME = ProcessType(GroupWaitingNameProcessData::class.java, "GROUP_WAITING_NAME", ProcessorType.REPLY_MESSAGE, ServerBotGroup::waitNameProcessor)
    val GROUP_SELECT_TOPIC_FEATURE = ProcessType(GroupSelectTopicProcessData::class.java, "GROUP_SELECT_TOPIC_FEATURE")
    val DUMMY = ProcessType(ProcessData::class.java, "DUMMY")

    val entries = hashMapOf<String, ProcessType<*>>(
        GROUP_WAITING_NAME.serializedName to GROUP_WAITING_NAME,
        GROUP_SELECT_TOPIC_FEATURE.serializedName to GROUP_SELECT_TOPIC_FEATURE,
        DUMMY.serializedName to DUMMY,
    )
    fun registerType(type: ProcessType<*>) {
        entries[type.serializedName] = type
    }
}
