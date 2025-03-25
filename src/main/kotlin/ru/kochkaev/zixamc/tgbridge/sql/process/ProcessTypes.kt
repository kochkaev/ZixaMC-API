package ru.kochkaev.zixamc.tgbridge.sql.process

import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup
import ru.kochkaev.zixamc.tgbridge.telegram.feature.type.ChatSyncFeatureType
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.integration.Menu

object ProcessTypes {
    val GROUP_WAITING_NAME = ProcessType(GroupWaitingNameProcessData::class.java, "GROUP_WAITING_NAME", ProcessorType.REPLY_MESSAGE, ServerBotGroup::waitNameProcessor)
    val GROUP_SELECT_TOPIC_FEATURE = ProcessType(GroupSelectTopicProcessData::class.java, "GROUP_SELECT_TOPIC_FEATURE")
    val GROUP_CHATSYNC_WAITING_PREFIX = ProcessType(GroupChatSyncWaitPrefixProcessData::class.java, "GROUP_CHATSYNC_WAITING_PREFIX", ProcessorType.REPLY_MESSAGE, ChatSyncFeatureType::waitPrefixProcessor)
    val MENU_AUDIO_PLAYER_UPLOAD = ProcessType(ProcessData::class.java, "MENU_AUDIO_PLAYER_UPLOAD", ProcessorType.REPLY_MESSAGE, Menu::audioPlayerProcessor)
    val DUMMY = ProcessType(ProcessData::class.java, "DUMMY")

    val entries = hashMapOf<String, ProcessType<*>>(
        GROUP_WAITING_NAME.serializedName to GROUP_WAITING_NAME,
        GROUP_SELECT_TOPIC_FEATURE.serializedName to GROUP_SELECT_TOPIC_FEATURE,
        GROUP_CHATSYNC_WAITING_PREFIX.serializedName to GROUP_CHATSYNC_WAITING_PREFIX,
        MENU_AUDIO_PLAYER_UPLOAD.serializedName to MENU_AUDIO_PLAYER_UPLOAD,
        DUMMY.serializedName to DUMMY,
    )
    fun registerType(type: ProcessType<*>) {
        entries[type.serializedName] = type
    }
}
