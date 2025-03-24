package ru.kochkaev.zixamc.tgbridge.sql.process

object ProcessTypes {
    val GROUP_WAITING_NAME = ProcessType(GroupWaitingNameProcessData::class.java, "GROUP_WAITING_NAME")
    val GROUP_SELECT_TOPIC_FEATURE = ProcessType(GroupSelectTopicProcessData::class.java, "GROUP_SELECT_TOPIC_FEATURE")
    val GROUP_CHATSYNC_WAITING_PREFIX = ProcessType(GroupChatSyncWaitPrefixProcessData::class.java, "GROUP_CHATSYNC_WAITING_PREFIX")
    val MENU_AUDIO_PLAYER_UPLOAD = ProcessType(ProcessData::class.java, "MENU_AUDIO_PLAYER_UPLOAD")
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
