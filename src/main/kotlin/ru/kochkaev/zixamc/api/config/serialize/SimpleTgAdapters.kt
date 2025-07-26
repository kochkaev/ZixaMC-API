package ru.kochkaev.zixamc.api.config.serialize

import ru.kochkaev.zixamc.api.telegram.model.TgBackgroundFill
import ru.kochkaev.zixamc.api.telegram.model.TgBackgroundFillType
import ru.kochkaev.zixamc.api.telegram.model.TgBackgroundType
import ru.kochkaev.zixamc.api.telegram.model.TgBackgroundTypes
import ru.kochkaev.zixamc.api.telegram.model.TgChatBoostSource
import ru.kochkaev.zixamc.api.telegram.model.TgChatBoostSources
import ru.kochkaev.zixamc.api.telegram.model.TgChatMember
import ru.kochkaev.zixamc.api.telegram.model.TgChatMemberStatuses
import ru.kochkaev.zixamc.api.telegram.model.TgInputMedia
import ru.kochkaev.zixamc.api.telegram.model.TgInputMediaType
import ru.kochkaev.zixamc.api.telegram.model.TgInputPaidMedia
import ru.kochkaev.zixamc.api.telegram.model.TgInputPaidMediaType
import ru.kochkaev.zixamc.api.telegram.model.TgInputProfilePhoto
import ru.kochkaev.zixamc.api.telegram.model.TgInputProfilePhotoType
import ru.kochkaev.zixamc.api.telegram.model.TgInputStoryContent
import ru.kochkaev.zixamc.api.telegram.model.TgInputStoryContentType
import ru.kochkaev.zixamc.api.telegram.model.TgMessageOrigin
import ru.kochkaev.zixamc.api.telegram.model.TgMessageOriginType
import ru.kochkaev.zixamc.api.telegram.model.TgOwnedGift
import ru.kochkaev.zixamc.api.telegram.model.TgOwnedGiftType
import ru.kochkaev.zixamc.api.telegram.model.TgPaidMedia
import ru.kochkaev.zixamc.api.telegram.model.TgPaidMediaType
import ru.kochkaev.zixamc.api.telegram.model.TgPassportElementError
import ru.kochkaev.zixamc.api.telegram.model.TgPassportElementErrorSource
import ru.kochkaev.zixamc.api.telegram.model.TgReactionType
import ru.kochkaev.zixamc.api.telegram.model.TgReactionTypes
import ru.kochkaev.zixamc.api.telegram.model.TgRevenueWithdrawalState
import ru.kochkaev.zixamc.api.telegram.model.TgRevenueWithdrawalStateType
import ru.kochkaev.zixamc.api.telegram.model.TgTransactionPartner
import ru.kochkaev.zixamc.api.telegram.model.TgTransactionPartnerType

class TgChatMemberAdapter: SimpleTgAdapter<TgChatMember>(
    typeField = "status",
    typeModel = TgChatMemberStatuses::class.java
)
class TgReactionTypeAdapter: SimpleTgAdapter<TgReactionType>(
    typeField = "type",
    typeModel = TgReactionTypes::class.java
)
class TgMessageOriginAdapter: SimpleTgAdapter<TgMessageOrigin>(
    typeField = "type",
    typeModel = TgMessageOriginType::class.java
)
class TgPaidMediaAdapter: SimpleTgAdapter<TgPaidMedia>(
    typeField = "type",
    typeModel = TgPaidMediaType::class.java
)
class TgRevenueWithdrawalStateAdapter: SimpleTgAdapter<TgRevenueWithdrawalState>(
    typeField = "type",
    typeModel = TgRevenueWithdrawalStateType::class.java
)
class TgTransactionPartnerAdapter: SimpleTgAdapter<TgTransactionPartner>(
    typeField = "type",
    typeModel = TgTransactionPartnerType::class.java
)
class TgOwnedGiftAdapter: SimpleTgAdapter<TgOwnedGift>(
    typeField = "type",
    typeModel = TgOwnedGiftType::class.java
)
class TgPassportElementErrorAdapter: SimpleTgAdapter<TgPassportElementError>(
    typeField = "source",
    typeModel = TgPassportElementErrorSource::class.java
)
class TgBackgroundTypeAdapter: SimpleTgAdapter<TgBackgroundType>(
    typeField = "type",
    typeModel = TgBackgroundTypes::class.java
)
class TgBackgroundFillAdapter: SimpleTgAdapter<TgBackgroundFill>(
    typeField = "type",
    typeModel = TgBackgroundFillType::class.java
)
class TgChatBoostSourceAdapter: SimpleTgAdapter<TgChatBoostSource>(
    typeField = "source",
    typeModel = TgChatBoostSources::class.java
)
class TgInputMediaAdapter: SimpleTgAdapter<TgInputMedia>(
    typeField = "type",
    typeModel = TgInputMediaType::class.java
)
class TgInputPaidMediaAdapter: SimpleTgAdapter<TgInputPaidMedia>(
    typeField = "type",
    typeModel = TgInputPaidMediaType::class.java
)
class TgInputProfilePhotoAdapter: SimpleTgAdapter<TgInputProfilePhoto>(
    typeField = "type",
    typeModel = TgInputProfilePhotoType::class.java
)
class TgInputStoryContentAdapter: SimpleTgAdapter<TgInputStoryContent>(
    typeField = "type",
    typeModel = TgInputStoryContentType::class.java
)
