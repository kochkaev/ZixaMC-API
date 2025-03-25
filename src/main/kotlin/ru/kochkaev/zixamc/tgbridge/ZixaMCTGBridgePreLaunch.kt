package ru.kochkaev.zixamc.tgbridge

import com.google.common.reflect.TypeToken
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import org.apache.logging.log4j.Level
import ru.kochkaev.zixamc.tgbridge.config.ConfigManager
import ru.kochkaev.zixamc.tgbridge.sql.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import ru.kochkaev.zixamc.tgbridge.config.GsonManager
import ru.kochkaev.zixamc.tgbridge.config.TextData
import ru.kochkaev.zixamc.tgbridge.config.serialize.*
import ru.kochkaev.zixamc.tgbridge.sql.callback.TgCallback
import ru.kochkaev.zixamc.tgbridge.sql.data.AccountType
import ru.kochkaev.zixamc.tgbridge.sql.process.ProcessType
import ru.kochkaev.zixamc.tgbridge.sql.util.*
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBotGroup
import ru.kochkaev.zixamc.tgbridge.telegram.feature.data.FeatureData
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureType
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgChatMember
import ru.kochkaev.zixamc.tgbridge.telegram.serverBot.group.ConsoleLogAppender

/**
 * @author kochkaev
 */
class ZixaMCTGBridgePreLaunch : PreLaunchEntrypoint {
    companion object {
        fun registerTypeAdapters() {
            GsonManager.registerAdapters(
//                AccountType::class.java to AccountTypeAdapter(),
                TgCallback::class.java to CallbackDataAdapter(),
                object : TypeToken<Map<FeatureType<out FeatureData>, FeatureData>>() {}.type to FeatureMapDeserializer(),
                FeatureType::class.java to FeatureTypeAdapter(),
                LinkedCallback::class.java to LinkedCallbackAdapter(),
                LinkedGroup::class.java to LinkedGroupAdapter(),
                LinkedUser::class.java to LinkedUserAdapter(),
                ProcessType::class.java to ProcessTypeAdapter(),
                TextData::class.java to TextDataAdapter(),
                TgChatMember::class.java to TgChatMemberAdapter(),
                ServerBotGroup.SetupFeatureCallback::class.java to SetupFeatureCallbackAdapter(),
                ServerBotGroup.GroupCallback::class.java to GroupCallbackAdapter(),
            )
        }
    }
    override fun onPreLaunch() {
        registerTypeAdapters()
        ConfigManager.init()

//        MySQL.connect()
//        SQLCallback.connectTable()
//        SQLProcess.connectTable()
//        SQLEntity.connectTable()
//        SQLGroup.connectTable()
        Initializer.startSQL()
    }
}
