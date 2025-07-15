package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.serialize.TgChatBoostSourceAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTypeEnum

/** This object describes the source of a chat boost. It can be one of ChatBoostSourcePremium, ChatBoostSourceGiftCode or ChatBoostSourceGiveaway */
@JsonAdapter(TgChatBoostSourceAdapter::class)
open class TgChatBoostSource(
    /** Source of the boost */
    val source: TgChatBoostSources,
)
enum class TgChatBoostSources: TgTypeEnum {
    @SerializedName("premium")
    PREMIUM {
        override val model = TgChatBoostSourcePremium::class.java
    },
    @SerializedName("gift_code")
    GIFT_CODE {
        override val model = TgChatBoostSourceGiftCode::class.java
    },
    @SerializedName("giveaway")
    GIVEAWAY {
        override val model = TgChatBoostSourceGiveaway::class.java
    },
}
/** The boost was obtained by subscribing to Telegram Premium or by gifting a Telegram Premium subscription to another user. */
class TgChatBoostSourcePremium(
    /** User that boosted the chat */
    val user: TgUser,
): TgChatBoostSource(TgChatBoostSources.PREMIUM)
/** The boost was obtained by the creation of Telegram Premium gift codes to boost a chat. Each such code boosts the chat 4 times for the duration of the corresponding Telegram Premium subscription. */
class TgChatBoostSourceGiftCode(
    /** User for which the gift code was created */
    val user: TgUser,
): TgChatBoostSource(TgChatBoostSources.GIFT_CODE)
/** The boost was obtained by the creation of a Telegram Premium or a Telegram Star giveaway. This boosts the chat 4 times for the duration of the corresponding Telegram Premium subscription for Telegram Premium giveaways and prize_star_count / 500 times for one year for Telegram Star giveaways. */
class TgChatBoostSourceGiveaway(
    /** Identifier of a message in the chat with the giveaway; the message could have been deleted already. May be 0 if the message isn't sent yet. */
    @SerializedName("giveaway_message_id")
    val giveawayMessageId: Int,
    /** User that won the prize in the giveaway if any; for Telegram Premium giveaways only */
    val user: TgUser?,
    /** The number of Telegram Stars to be split between giveaway winners; for Telegram Star giveaways only */
    @SerializedName("prize_star_count")
    val prizeStarCount: Int?,
    /** True, if the giveaway was completed, but there was no user to win the prize */
    @SerializedName("is_unclaimed")
    val isUnclaimed: Boolean?,
): TgChatBoostSource(TgChatBoostSources.GIVEAWAY)

/** This object contains information about a chat boost. */
data class TgChatBoost(
    /** Unique identifier of the boost */
    @SerializedName("boost_id")
    val boostId: String,
    /** Point in time (Unix timestamp) when the chat was boosted */
    @SerializedName("addDate")
    val addDate: Int,
    /** Point in time (Unix timestamp) when the boost will automatically expire, unless the booster's Telegram Premium subscription is prolonged */
    @SerializedName("expirationDate")
    val expirationDate: Int,
    /** Source of the added boost */
    val source: TgChatBoostSource,
)

/** This object represents a boost added to a chat or changed. */
data class TgChatBoostUpdated(
    /** Chat which was boosted */
    val chat: TgChat,
    /** Information about the chat boost */
    val boost: TgChatBoost
)
/** This object represents a service message about a user boosting a chat. */
data class TgChatBoostAdded(
    /** Number of boosts added by the user */
    @SerializedName("boost_count")
    val boostCount: Int
)

/** This object represents a boost removed from a chat. */
data class TgChatBoostRemoved(
    /** Chat which was boosted */
    val chat: TgChat,
    /** Unique identifier of the boost */
    @SerializedName("boost_id")
    val boostId: String,
    /** Point in time (Unix timestamp) when the boost was removed */
    @SerializedName("remove_date")
    val removeDate: Int,
    /** Source of the removed boost */
    val source: TgChatBoostSource
)
/** This object represents a list of boosts added to a chat by a user. */
data class TgUserChatBoosts(
    /** The list of boosts added to the chat by the user */
    val boosts: List<TgChatBoost>
)