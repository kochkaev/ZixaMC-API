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
import ru.kochkaev.zixamc.api.config.serialize.ChatDataMapDeserializer
import ru.kochkaev.zixamc.api.config.serialize.ChatDataTypeAdapter
import ru.kochkaev.zixamc.api.config.serialize.TempConfigMapDeserializer
import ru.kochkaev.zixamc.api.config.serialize.TempConfigValueTypeAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgBackgroundFillAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgBackgroundTypeAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgChatBoostSourceAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgChatMemberAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgInputMediaAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgInputPaidMediaAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgInputProfilePhotoAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgInputStoryContentAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgMaybeInaccessibleMessageAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgMessageOriginAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgOwnedGiftAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgPaidMediaAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgPassportElementErrorAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgReactionTypeAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgRevenueWithdrawalStateAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTransactionPartnerAdapter
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
import ru.kochkaev.zixamc.api.telegram.model.TgBackgroundFill
import ru.kochkaev.zixamc.api.telegram.model.TgBackgroundType
import ru.kochkaev.zixamc.api.telegram.model.TgChatBoostSource
import ru.kochkaev.zixamc.api.telegram.model.TgInputMedia
import ru.kochkaev.zixamc.api.telegram.model.TgInputPaidMedia
import ru.kochkaev.zixamc.api.telegram.model.TgInputProfilePhoto
import ru.kochkaev.zixamc.api.telegram.model.TgInputStoryContent
import ru.kochkaev.zixamc.api.telegram.model.TgMaybeInaccessibleMessage
import ru.kochkaev.zixamc.api.telegram.model.TgMessageOrigin
import ru.kochkaev.zixamc.api.telegram.model.TgOwnedGift
import ru.kochkaev.zixamc.api.telegram.model.TgPaidMedia
import ru.kochkaev.zixamc.api.telegram.model.TgPassportElementError
import ru.kochkaev.zixamc.api.telegram.model.TgReactionType
import ru.kochkaev.zixamc.api.telegram.model.TgRevenueWithdrawalState
import ru.kochkaev.zixamc.api.telegram.model.TgTransactionPartner

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
                TgReactionType::class.java to TgReactionTypeAdapter(),
                TgMessageOrigin::class.java to TgMessageOriginAdapter(),
                TgPaidMedia::class.java to TgPaidMediaAdapter(),
                TgRevenueWithdrawalState::class.java to TgRevenueWithdrawalStateAdapter(),
                TgTransactionPartner::class.java to TgTransactionPartnerAdapter(),
                TgOwnedGift::class.java to TgOwnedGiftAdapter(),
                TgPassportElementError::class.java to TgPassportElementErrorAdapter(),
                TgBackgroundType::class.java to TgBackgroundTypeAdapter(),
                TgBackgroundFill::class.java to TgBackgroundFillAdapter(),
                TgChatBoostSource::class.java to TgChatBoostSourceAdapter(),
                TgInputMedia::class.java to TgInputMediaAdapter(),
                TgInputPaidMedia::class.java to TgInputPaidMediaAdapter(),
                TgInputProfilePhoto::class.java to TgInputProfilePhotoAdapter(),
                TgInputStoryContent::class.java to TgInputStoryContentAdapter(),
                TgMaybeInaccessibleMessage::class.java to TgMaybeInaccessibleMessageAdapter(),
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
    }
}
