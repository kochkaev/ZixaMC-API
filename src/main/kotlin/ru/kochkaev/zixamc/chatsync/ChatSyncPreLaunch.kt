package ru.kochkaev.zixamc.chatsync

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import ru.kochkaev.zixamc.api.sql.feature.FeatureTypes
import ru.kochkaev.zixamc.api.sql.process.ProcessTypes

class ChatSyncPreLaunch: PreLaunchEntrypoint {

    override fun onPreLaunch() {
        FeatureTypes.registerType(ChatSyncFeatureType)
        ProcessTypes.registerType(ChatSyncWaitingPrefixProcess)
    }

}