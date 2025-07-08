package ru.kochkaev.zixamc.api

import com.google.common.reflect.TypeToken
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import ru.kochkaev.zixamc.api.config.Config
import ru.kochkaev.zixamc.api.config.serialize.GroupCallbackAdapter
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.config.GsonManager
import ru.kochkaev.zixamc.api.config.TempConfig
import ru.kochkaev.zixamc.api.config.TempConfigValueType
import ru.kochkaev.zixamc.api.config.serialize.MenuCallbackDataAdapter
import ru.kochkaev.zixamc.api.config.TextData
import ru.kochkaev.zixamc.api.config.serialize.AdminPanelCallbackAdapter
import ru.kochkaev.zixamc.api.config.serialize.CallbackDataAdapter
import ru.kochkaev.zixamc.api.config.serialize.FeatureMapDeserializer
import ru.kochkaev.zixamc.api.config.serialize.FeatureTypeAdapter
import ru.kochkaev.zixamc.api.config.serialize.ProcessTypeAdapter
import ru.kochkaev.zixamc.api.config.serialize.SQLCallbackAdapter
import ru.kochkaev.zixamc.api.config.serialize.SQLGroupAdapter
import ru.kochkaev.zixamc.api.config.serialize.SQLUserAdapter
import ru.kochkaev.zixamc.api.config.serialize.SetupFeatureCallbackAdapter
import ru.kochkaev.zixamc.api.config.serialize.TextDataAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgChatMemberAdapter
import ru.kochkaev.zixamc.api.config.serialize.ChatDataMapDeserializer
import ru.kochkaev.zixamc.api.config.serialize.ChatDataTypeAdapter
import ru.kochkaev.zixamc.api.config.serialize.TempConfigMapDeserializer
import ru.kochkaev.zixamc.api.config.serialize.TempConfigValueTypeAdapter
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.callback.TgCallback
import ru.kochkaev.zixamc.api.sql.process.ProcessType
import ru.kochkaev.zixamc.api.telegram.ServerBotGroup
import ru.kochkaev.zixamc.api.sql.feature.data.FeatureData
import ru.kochkaev.zixamc.api.sql.feature.FeatureType
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataType
import ru.kochkaev.zixamc.api.telegram.AdminPanel
import ru.kochkaev.zixamc.api.telegram.model.TgChatMember
import ru.kochkaev.zixamc.api.telegram.Menu
import ru.kochkaev.zixamc.chatsync.ChatSyncPreLaunch

/**
 * @author kochkaev
 */
class ZixaMCPreLaunch : PreLaunchEntrypoint {
    companion object {
        fun registerTypeAdapters() {
            GsonManager.registerAdapters(
//                AccountType::class.java to AccountTypeAdapter(),
                TgCallback::class.java to CallbackDataAdapter(),
                Menu.MenuCallbackData::class.java to MenuCallbackDataAdapter(),
                AdminPanel.AdminPanelCallback::class.java to AdminPanelCallbackAdapter(),
                object : TypeToken<Map<FeatureType<out FeatureData>, FeatureData>>() {}.type to FeatureMapDeserializer(),
                object : TypeToken<Map<ChatDataType<*>, *>>() {}.type to ChatDataMapDeserializer(),
                object : TypeToken<Map<TempConfigValueType<*>, *>>() {}.type to TempConfigMapDeserializer(),
                SQLUser::class.java to SQLUserAdapter(),
                SQLGroup::class.java to SQLGroupAdapter(),
                SQLCallback::class.java to SQLCallbackAdapter(),
                TextData::class.java to TextDataAdapter(),
                TgChatMember::class.java to TgChatMemberAdapter(),
                ServerBotGroup.SetupFeatureCallback::class.java to SetupFeatureCallbackAdapter(),
                ServerBotGroup.GroupCallback::class.java to GroupCallbackAdapter(),
            )
            GsonManager.registerHierarchyAdapters(
                FeatureType::class.java to FeatureTypeAdapter(),
                ProcessType::class.java to ProcessTypeAdapter(),
                ChatDataType::class.java to ChatDataTypeAdapter(),
                TempConfigValueType::class.java to TempConfigValueTypeAdapter(),
            )
        }
    }
    override fun onPreLaunch() {
        registerTypeAdapters()
        ConfigManager.registerConfig(Config)
        TempConfig.temp.init()

        Initializer.startSQL()

        ChatSyncPreLaunch().onPreLaunch()
    }
}
