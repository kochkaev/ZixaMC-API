package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents a message about a scheduled giveaway. */
data class TgGiveaway(
    /** The list of chats which the user must join to participate in the giveaway */
    val chats: List<TgChat>,
    /** Point in time (Unix timestamp) when winners of the giveaway will be selected */
    @SerializedName("winners_selection_date")
    val winnersSelectionDate: Int,
    /** The number of users which are supposed to be selected as winners of the giveaway */
    @SerializedName("winner_count")
    val winnerCount: Int,
    /** True, if only users who join the chats after the giveaway started should be eligible to win */
    @SerializedName("only_new_members")
    val onlyNewMembers: Boolean?,
    /** True, if the list of giveaway winners will be visible to everyone */
    @SerializedName("has_public_winners")
    val hasPublicWinners: Boolean?,
    /** Description of additional giveaway prize */
    @SerializedName("prize_description")
    val prizeDescription: String?,
    /** A list of two-letter ISO 3166-1 alpha-2 country codes indicating the countries from which eligible users for the giveaway must come. If empty, then all users can participate in the giveaway. Users with a phone number that was bought on Fragment can always participate in giveaways. */
    @SerializedName("country_codes")
    val countryCodes: List<String>?,
    /** The number of Telegram Stars to be split between giveaway winners; for Telegram Star giveaways only */
    @SerializedName("prize_star_count")
    val prizeStarCount: Int?,
    /** The number of months the Telegram Premium subscription won from the giveaway will be active for; for Telegram Premium giveaways only */
    @SerializedName("premium_subscription_month_count")
    val premiumSubscriptionMonthCount: Int?,
)
/** This object represents a message about the completion of a giveaway with public winners. */
data class TgGiveawayWinners(
    /** The chat that created the giveaway */
    val chat: TgChat,
    /** Identifier of the message with the giveaway in the chat */
    @SerializedName("giveaway_message_id")
    val giveawayMessageId: Int,
    /** Point in time (Unix timestamp) when winners of the giveaway were selected */
    @SerializedName("winners_selection_date")
    val winnersSelectionDate: Int,
    /** Total number of winners in the giveaway */
    @SerializedName("winner_count")
    val winnerCount: Int,
    /** List of up to 100 winners of the giveaway */
    val winners: List<TgUser>,
    /** The number of other chats the user had to join in order to be eligible for the giveaway */
    @SerializedName("additional_chat_count")
    val additionalChatCount: Int?,
    /** The number of Telegram Stars that were split between giveaway winners; for Telegram Star giveaways only */
    @SerializedName("prize_star_count")
    val prizeStarCount: Int?,
    /** The number of months the Telegram Premium subscription won from the giveaway will be active for; for Telegram Premium giveaways only */
    @SerializedName("premium_subscription_month_count")
    val premiumSubscriptionMonthCount: Int?,
    /** Number of undistributed prizes */
    @SerializedName("unclaimed_prize_count")
    val unclaimedPrizeCount: Int?,
    /** True, if only users who had joined the chats after the giveaway started were eligible to win */
    @SerializedName("only_new_members")
    val onlyNewMembers: Boolean?,
    /** True, if the giveaway was canceled because the payment for it was refunded */
    @SerializedName("was_refunded")
    val wasRefunded: Boolean?,
    /** Description of additional giveaway prize */
    @SerializedName("prize_description")
    val prizeDescription: String?,
)
/** This object represents a service message about the completion of a giveaway without public winners. */
data class TgGiveawayCompleted(
    /** Number of winners in the giveaway */
    @SerializedName("winner_count")
    val winnerCount: Int,
    /** Number of undistributed prizes */
    @SerializedName("unclaimed_prize_count")
    val unclaimedPrizeCount: Int,
    /** Message with the giveaway that was completed, if it wasn't deleted */
    @SerializedName("giveaway_message")
    val giveawayMessage: TgMessage?,
    /** True, if the giveaway is a Telegram Star giveaway. Otherwise, currently, the giveaway is a Telegram Premium giveaway. */
    @SerializedName("is_star_giveaway")
    val isStarGiveaway: TgMessage?,
)
/** This object represents a service message about the creation of a scheduled giveaway. */
data class TgGiveawayCreated(
    /** The number of Telegram Stars to be split between giveaway winners; for Telegram Star giveaways only */
    @SerializedName("prize_star_count")
    val prizeStarCount: Int?,
)
