package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.serialize.TgOwnedGiftAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTypeEnum

/** This object represents a gift that can be sent by the bot. */
data class TgGift(
    /** Unique identifier of the gift */
    val id: String,
    /** The sticker that represents the gift */
    val sticker: TgSticker,
    /** The number of Telegram Stars that must be paid to send the sticker */
    @SerializedName("star_count")
    val starCount: Int,
    /** The number of Telegram Stars that must be paid to upgrade the gift to a unique one */
    @SerializedName("upgrade_star_count")
    val upgradeStarCount: Int?,
    /** The total number of the gifts of this type that can be sent; for limited gifts only */
    @SerializedName("total_count")
    val totalCount: Int?,
    /** The number of remaining gifts of this type that can be sent; for limited gifts only */
    @SerializedName("remaining_count")
    val remainingCount: Int?,
)
/** This object represent a list of gifts. */
data class TgGifts(
    /** The list of gifts */
    val gifts: List<TgGift>
)
/** This object describes the model of a unique gift. */
data class TgUniqueGiftModel(
    /** Name of the model */
    val name: String,
    /** The sticker that represents the unique gift */
    val sticker: TgSticker,
    /** The number of unique gifts that receive this model for every 1000 gifts upgraded */
    @SerializedName("rarity_per_mille")
    val rarityPerMille: Int,
)
/** This object describes the symbol shown on the pattern of a unique gift. */
data class TgUniqueGiftSymbol(
    /** Name of the symbol */
    val name: String,
    /** The sticker that represents the unique gift */
    val sticker: TgSticker,
    /** The number of unique gifts that receive this model for every 1000 gifts upgraded */
    @SerializedName("rarity_per_mille")
    val rarityPerMille: Int,
)
/** This object describes the colors of the backdrop of a unique gift. */
data class TgUniqueGiftBackdropColors(
    /** The color in the center of the backdrop in RGB format */
    @SerializedName("center_color")
    val centerColor: Int,
    /** The color on the edges of the backdrop in RGB format */
    @SerializedName("edge_color")
    val edgeColor: Int,
    /** The color to be applied to the symbol in RGB format */
    @SerializedName("symbol_color")
    val symbolColor: Int,
    /** The color for the text on the backdrop in RGB format */
    @SerializedName("text_color")
    val textColor: Int,
)
/** This object describes the symbol shown on the pattern of a unique gift. */
data class TgUniqueGiftBackdrop(
    /** Name of the symbol */
    val name: String,
    /** Colors of the backdrop */
    val colors: TgUniqueGiftBackdropColors,
    /** The number of unique gifts that receive this model for every 1000 gifts upgraded */
    @SerializedName("rarity_per_mille")
    val rarityPerMille: Int,
)
/** This object describes a unique gift that was upgraded from a regular gift. */
data class TgUniqueGift(
    /** Human-readable name of the regular gift from which this unique gift was upgraded */
    @SerializedName("base_name")
    val baseName: String,
    /** Unique name of the gift. This name can be used in https://t.me/nft/... links and story areas */
    val name: String,
    /** Unique number of the upgraded gift among gifts upgraded from the same regular gift */
    val number: Int,
    /** Model of the gift */
    val model: TgUniqueGiftModel,
    /** Symbol of the gift */
    val symbol: TgUniqueGiftSymbol,
    /** Backdrop of the gift */
    val backdrop: TgUniqueGiftBackdrop,
)
/** Describes a service message about a regular gift that was sent or received. */
data class TgGiftInfo(
    /** Information about the gift */
    val gift: TgGift,
    /** Unique identifier of the received gift for the bot; only present for gifts received on behalf of business accounts */
    @SerializedName("owned_gift_id")
    val ownedGiftId: String?,
    /** Number of Telegram Stars that can be claimed by the receiver by converting the gift; omitted if conversion to Telegram Stars is impossible */
    @SerializedName("convert_star_count")
    val convertStarCount: Int?,
    /** Number of Telegram Stars that were prepaid by the sender for the ability to upgrade the gift */
    @SerializedName("prepaid_upgrade_star_count")
    val prepaidUpgradeStarCount: Int?,
    /** True, if the gift can be upgraded to a unique gift */
    @SerializedName("can_be_upgraded")
    val canBeUpgraded: Boolean?,
    /** Text of the message that was added to the gift */
    val text: String?,
    /** Special entities that appear in the text */
    val entities: List<TgEntity>?,
    /** True, if the sender and gift text are shown only to the gift receiver; otherwise, everyone will be able to see them */
    @SerializedName("is_private")
    val isPrivate: List<TgEntity>?,
)
enum class TgGiftOrigin {
    @SerializedName("upgrade")
    UPGRADE,
    @SerializedName("transfer")
    TRANSFER,
    @SerializedName("resale")
    RESALE,
}
/** Describes a service message about a unique gift that was sent or received. */
data class TgUniqueGiftInfo(
    /** Information about the gift */
    val gift: TgUniqueGift,
    /** Origin of the gift. Currently, either “upgrade” for gifts upgraded from regular gifts, “transfer” for gifts transferred from other users or channels, or “resale” for gifts bought from other users */
    val origin: TgGiftOrigin,
    /** For gifts bought from other users, the price paid for the gift */
    @SerializedName("last_resale_star_count")
    val lastResaleStarCount: Int?,
    /** Unique identifier of the received gift for the bot; only present for gifts received on behalf of business accounts */
    @SerializedName("owned_gift_id")
    val ownedGiftId: String?,
    /** Number of Telegram Stars that must be paid to transfer the gift; omitted if the bot cannot transfer the gift */
    @SerializedName("transfer_star_count")
    val transfersSarCount: Int?,
    /** Point in time (Unix timestamp) when the gift can be transferred. If it is in the past, then the gift can be transferred now */
    @SerializedName("next_transfer_date")
    val nextTransferDate: Int?,
)
/** This object describes a gift received and owned by a user or a chat. Currently, it can be one of OwnedGiftRegular or OwnedGiftUnique */
@JsonAdapter(TgOwnedGiftAdapter::class)
open class TgOwnedGift(
    /** Type of the gift */
    val type: TgOwnedGiftType,/** Unique identifier of the gift for the bot; for gifts received on behalf of business accounts only */
    @SerializedName("owned_gift_id")
    val ownedGiftId: String?,
    /** Sender of the gift if it is a known user */
    @SerializedName("sender_user")
    val senderUser: TgUser?,
    /** Date the gift was sent in Unix time */
    @SerializedName("send_date")
    val sendDate: Int?,
    /** True, if the gift is displayed on the account's profile page; for gifts received on behalf of business accounts only */
    @SerializedName("is_saved")
    val isSaved: Boolean?,
)
enum class TgOwnedGiftType: TgTypeEnum {
    @SerializedName("regular")
    REGULAR {
        override val model = TgOwnedGiftRegular::class.java
    },
    @SerializedName("unique")
    UNIQUE {
        override val model = TgOwnedGiftUnique::class.java
    },
}
/** Describes a regular gift owned by a user or a chat. */
class TgOwnedGiftRegular(
    /** Information about the regular gift */
    val gift: TgGift,
    ownedGiftId: String?,
    senderUser: TgUser?,
    sendDate: Int?,
    /** Text of the message that was added to the gift */
    val text: String?,
    /** Special entities that appear in the text */
    val entities: List<TgEntity>?,
    /** True, if the sender and gift text are shown only to the gift receiver; otherwise, everyone will be able to see them */
    @SerializedName("is_private")
    val isPrivate: Boolean?,
    isSaved: Boolean?,
    /** True, if the gift can be upgraded to a unique gift; for gifts received on behalf of business accounts only */
    @SerializedName("can_be_upgraded")
    val canBeUpgraded: Boolean?,
    /** True, if the gift was refunded and isn't available anymore */
    @SerializedName("was_refunded")
    val wasRefunded: Boolean?,
    /** Number of Telegram Stars that can be claimed by the receiver instead of the gift; omitted if the gift cannot be converted to Telegram Stars */
    @SerializedName("convert_star_count")
    val convertStarCount: Int?,
    /** Number of Telegram Stars that were paid by the sender for the ability to upgrade the gift */
    @SerializedName("prepaid_upgrade_star_count")
    val prepaidUpgradeStarCount: Int?,
): TgOwnedGift(TgOwnedGiftType.REGULAR, ownedGiftId, senderUser, sendDate, isSaved)
/** Describes a regular gift owned by a user or a chat. */
class TgOwnedGiftUnique(
    /** Information about the unique gift */
    val gift: TgUniqueGift,
    ownedGiftId: String?,
    senderUser: TgUser?,
    sendDate: Int?,
    isSaved: Boolean?,
    /** True, if the gift can be transferred to another owner; for gifts received on behalf of business accounts only */
    @SerializedName("can_be_transferred")
    val canBeTransferred: Int?,
    /** Number of Telegram Stars that must be paid to transfer the gift; omitted if the bot cannot transfer the gift */
    @SerializedName("transfer_star_count")
    val transfersSarCount: Int?,
    /** Point in time (Unix timestamp) when the gift can be transferred. If it is in the past, then the gift can be transferred now */
    @SerializedName("next_transfer_date")
    val nextTransferDate: Int?,
): TgOwnedGift(TgOwnedGiftType.UNIQUE, ownedGiftId, senderUser, sendDate, isSaved)
/** Contains the list of gifts received and owned by a user or a chat. */
data class TgOwnedGifts(
    /** The total number of gifts owned by the user or the chat */
    @SerializedName("total_count")
    val totalCount: Int,
    /** The list of gifts */
    val gifts: List<TgOwnedGift>,
    /** Offset for the next request. If empty, then there are no more results */
    @SerializedName("next_offset")
    val nextOffset: String?,
)
/** This object describes the types of gifts that can be gifted to a user or a chat. */
data class TgAcceptedGiftTypes(
    /** True, if unlimited regular gifts are accepted */
    @SerializedName("unlimited_gifts")
    val unlimitedGifts: Boolean,
    /** True, if limited regular gifts are accepted */
    @SerializedName("limited_gifts")
    val limitedGifts: Boolean,
    /** True, if unique gifts or gifts that can be upgraded to unique for free are accepted */
    @SerializedName("unique_gifts")
    val uniqueGifts: Boolean,
    /** True, if a Telegram Premium subscription is accepted */
    @SerializedName("premium_subscription")
    val premiumSubscription: Boolean,
)
/** Describes an amount of Telegram Stars. */
data class TgStarAmount(
    /** Integer amount of Telegram Stars, rounded to 0; can be negative */
    @SerializedName("unlimited_gifts")
    val amount: Int,
    /** The number of 1/1000000000 shares of Telegram Stars; from -999999999 to 999999999; can be negative if and only if amount is non-positive */
    @SerializedName("nanostar_amount")
    val nanostarAmount: Int,
)