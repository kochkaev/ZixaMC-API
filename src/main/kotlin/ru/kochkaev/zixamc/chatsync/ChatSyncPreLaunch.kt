package ru.kochkaev.zixamc.chatsync

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.sql.feature.FeatureTypes
import ru.kochkaev.zixamc.api.sql.process.ProcessTypes
import ru.kochkaev.zixamc.chatsync.settings.ChatSyncFeatureType
import ru.kochkaev.zixamc.chatsync.settings.ChatSyncWaitingPrefixProcess

class ChatSyncPreLaunch: PreLaunchEntrypoint {

    override fun onPreLaunch() {
        ConfigManager.registerConfig(Config)
        FeatureTypes.registerType(ChatSyncFeatureType)
        ProcessTypes.registerType(ChatSyncWaitingPrefixProcess)
    }

}