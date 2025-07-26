package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.serialize.TgRevenueWithdrawalStateAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTransactionPartnerAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTypeEnum
import java.lang.reflect.Type

/** This object contains basic information about an invoice. */
data class TgInvoice(
    /** Product name */
    val title: String,
    /** Product description */
    val description: String,
    /** Unique bot deep-linking parameter that can be used to generate this invoice */
    @SerializedName("start_parameter")
    val startParameter: String,
    /** Three-letter ISO 4217 currency code, or “XTR” for payments in Telegram Stars */
    val currency: String,
    /** Total price in the smallest units of the currency (integer, not float/double). For example, for a price of US$ 1.45 pass amount = 145. See the exp parameter in https://core.telegram.org/bots/payments/currencies.json, it shows the number of digits past the decimal point for each currency (2 for the majority of currencies). */
    @SerializedName("total_amount")
    val totalAmount: Int,
)
/** This object represents a shipping address. */
data class TgShippingAddress(
    /** Two-letter ISO 3166-1 alpha-2 country code */
    @SerializedName("country_code")
    val countryCode: String,
    /** State, if applicable */
    val state: String,
    /** City */
    val city: String,
    /** First line for the address */
    @SerializedName("street_line1")
    val streetLine1: String,
    /** Second line for the address */
    @SerializedName("street_line2")
    val streetLine2: String,
    /** Address post code */
    @SerializedName("post_code")
    val postCode: String,
)
/** This object represents information about an order. */
data class TgOrderInfo(
    /** User name */
    val name: String?,
    /** User's phone number */
    @SerializedName("phone_number")
    val phoneNumber: String?,
    /** User email */
    val email: String?,
    /** User shipping address */
    @SerializedName("shipping_address")
    val shippingAddress: TgShippingAddress?,
)
/** This object represents one shipping option. */
data class TgShippingOption(
    /** Shipping option identifier */
    val id: String,
    /** Option title */
    val title: String,
    /** List of price portions */
    val prices: List<TgLabeledPrice>,
)
/** This object represents a portion of the price for goods or services. */
data class TgLabeledPrice(
    /** Portion label */
    val label: String,
    /** Price of the product in the smallest units of the currency (integer, not float/double). For example, for a price of US$ 1.45 pass amount = 145. See the exp parameter in https://core.telegram.org/bots/payments/currencies.json, it shows the number of digits past the decimal point for each currency (2 for the majority of currencies). */
    val amount: Int,
)
/** This object contains basic information about a successful payment. Note that if the buyer initiates a chargeback with the relevant payment provider following this transaction, the funds may be debited from your balance. This is outside of Telegram's control. */
data class TgSuccessfulPayment(
    /** Three-letter ISO 4217 currency code, or “XTR” for payments in Telegram Stars */
    val currency: String,
    /** Total price in the smallest units of the currency (integer, not float/double). For example, for a price of US$ 1.45 pass amount = 145. See the exp parameter in https://core.telegram.org/bots/payments/currencies.json, it shows the number of digits past the decimal point for each currency (2 for the majority of currencies). */
    @SerializedName("total_amount")
    val totalAmount: Int,
    /** Bot-specified invoice payload */
    @SerializedName("invoice_payload")
    val invoicePayload: String,
    /** Expiration date of the subscription, in Unix time; for recurring payments only */
    @SerializedName("subscription_expiration_date")
    val subscriptionExpirationDate: Int?,
    /** True, if the payment is a recurring payment for a subscription */
    @SerializedName("is_recurring")
    val isRecurring: Boolean?,
    /** True, if the payment is the first payment for a subscription */
    @SerializedName("is_first_recurring")
    val isFirstRecurring: Boolean?,
    /** Identifier of the shipping option chosen by the user */
    @SerializedName("shipping_option_id")
    val shippingOptionId: String?,
    /** Order information provided by the user */
    @SerializedName("order_info")
    val orderInfo: TgOrderInfo?,
    /** Telegram payment identifier */
    @SerializedName("telegram_payment_charge_id")
    val telegramPaymentChargeId: String,
    /** Provider payment identifier */
    @SerializedName("provider_payment_charge_id")
    val providerPaymentChargeId: String,
)
/** This object contains basic information about a refunded payment. */
data class TgRefundedPayment(
    /** Three-letter ISO 4217 currency code, or “XTR” for payments in Telegram Stars */
    val currency: String,
    /** Total price in the smallest units of the currency (integer, not float/double). For example, for a price of US$ 1.45 pass amount = 145. See the exp parameter in https://core.telegram.org/bots/payments/currencies.json, it shows the number of digits past the decimal point for each currency (2 for the majority of currencies). */
    @SerializedName("total_amount")
    val totalAmount: Int,
    /** Bot-specified invoice payload */
    @SerializedName("invoice_payload")
    val invoicePayload: String,
    /** Telegram payment identifier */
    @SerializedName("telegram_payment_charge_id")
    val telegramPaymentChargeId: String,
    /** Provider payment identifier */
    @SerializedName("provider_payment_charge_id")
    val providerPaymentChargeId: String?,
)
/** This object contains information about an incoming shipping query. */
data class TgShippingQuery(
    /** Unique query identifier */
    val id: String,
    /** User who sent the query */
    val from: TgUser,
    /** Bot-specified invoice payload */
    @SerializedName("invoice_payload")
    val invoicePayload: String,
    /** User specified shipping address */
    @SerializedName("shipping_address")
    val shippingAddress: TgShippingAddress?,
)
/** This object contains information about an incoming pre-checkout query. */
data class TgPreCheckoutQuery(
    /** Unique query identifier */
    val id: String,
    /** User who sent the query */
    val from: TgUser,
    /** Three-letter ISO 4217 currency code, or “XTR” for payments in Telegram Stars */
    val currency: String,
    /** Total price in the smallest units of the currency (integer, not float/double). For example, for a price of US$ 1.45 pass amount = 145. See the exp parameter in https://core.telegram.org/bots/payments/currencies.json, it shows the number of digits past the decimal point for each currency (2 for the majority of currencies). */
    @SerializedName("total_amount")
    val totalAmount: Int,
    /** Bot-specified invoice payload */
    @SerializedName("invoice_payload")
    val invoicePayload: String,
    /** Identifier of the shipping option chosen by the user */
    @SerializedName("shipping_option_id")
    val shippingOptionId: String?,
    /** Order information provided by the user */
    @SerializedName("order_info")
    val orderInfo: TgOrderInfo?,
)
/** This object contains information about a paid media purchase. */
data class TgPaidMediaPurchased(
    /** User who purchased the media */
    val user: TgUser,
    /** Bot-specified paid media payload */
    @SerializedName("paid_media_payload")
    val paidMediaPayload: TgUser,
)

/** This object describes the state of a revenue withdrawal operation. Currently, it can be one of RevenueWithdrawalStatePending, RevenueWithdrawalStateSucceeded or RevenueWithdrawalStateFailed */
@JsonAdapter(TgRevenueWithdrawalStateAdapter::class)
open class TgRevenueWithdrawalState(
    /** Type of the state */
    val type: TgRevenueWithdrawalStateType,
)
enum class TgRevenueWithdrawalStateType: TgTypeEnum {
    @SerializedName("pending")
    PENDING {
        override val model = TgRevenueWithdrawalStatePending::class.java
    },
    @SerializedName("succeeded")
    SUCCEEDED {
        override val model = TgRevenueWithdrawalStateSucceeded::class.java
    },
    @SerializedName("failed")
    FAILED {
        override val model = TgRevenueWithdrawalStateFailed::class.java
    },
}
/** The withdrawal is in progress. */
class TgRevenueWithdrawalStatePending: TgRevenueWithdrawalState(TgRevenueWithdrawalStateType.PENDING)
/** The withdrawal succeeded. */
class TgRevenueWithdrawalStateSucceeded(
    /** Date the withdrawal was completed in Unix time */
    val data: Int,
    /** An HTTPS URL that can be used to see transaction details */
    val url: String,
): TgRevenueWithdrawalState(TgRevenueWithdrawalStateType.SUCCEEDED)
/** The withdrawal failed and the transaction was refunded. */
class TgRevenueWithdrawalStateFailed: TgRevenueWithdrawalState(TgRevenueWithdrawalStateType.FAILED)

/** Contains information about the affiliate that received a commission via this transaction. */
data class TgAffiliateInfo(
    /** The bot or the user that received an affiliate commission if it was received by a bot or a user */
    @SerializedName("affiliate_user")
    val affiliateUser: TgUser?,
    /** The chat that received an affiliate commission if it was received by a chat */
    @SerializedName("affiliate_chat")
    val affiliateChat: TgChat?,
    /** The number of Telegram Stars received by the affiliate for each 1000 Telegram Stars received by the bot from referred users */
    @SerializedName("commission_per_mille")
    val commissionPerMille: Int,
    /** Integer amount of Telegram Stars received by the affiliate from the transaction, rounded to 0; can be negative for refunds */
    val amount: Int,
    /** Optional. The number of 1/1000000000 shares of Telegram Stars received by the affiliate; from -999999999 to 999999999; can be negative for refunds */
    @SerializedName("nanostar_amount")
    val nanostarAmount: Int,
)

/** This object describes the source of a transaction, or its recipient for outgoing transactions. Currently, it can be one of TransactionPartnerUser, TransactionPartnerChat, TransactionPartnerAffiliateProgram, TransactionPartnerFragment, TransactionPartnerTelegramAds, TransactionPartnerTelegramApi or TransactionPartnerOther */
@JsonAdapter(TgTransactionPartnerAdapter::class)
open class TgTransactionPartner(
    /** Type of the transaction partner */
    val type: TgTransactionPartnerType,
)
enum class TgTransactionPartnerType: TgTypeEnum {
    @SerializedName("user")
    USER {
        override val model = TgTransactionPartnerUser::class.java
    },
    @SerializedName("chat")
    CHAT {
        override val model = TgTransactionPartnerChat::class.java
    },
    @SerializedName("affiliate_program")
    AFFILIATE_PROGRAM {
        override val model = TgTransactionPartnerAffiliateProgram::class.java
    },
    @SerializedName("fragment")
    FRAGMENT {
        override val model = TgTransactionPartnerFragment::class.java
    },
    @SerializedName("telegram_ads")
    TELEGRAM_ADS {
        override val model = TgTransactionPartnerTelegramAds::class.java
    },
    @SerializedName("telegram_api")
    TELEGRAM_API {
        override val model = TgTransactionPartnerTelegramApi::class.java
    },
    @SerializedName("other")
    OTHER {
        override val model = TgTransactionPartnerOther::class.java
    },
}
enum class TgTransactionType {
    @SerializedName("invoice_payment")
    INVOICE_PAYMENT,
    @SerializedName("paid_media_payment")
    PAID_MEDIA_PAYMENT,
    @SerializedName("gift_purchase")
    GIFT_PURCHASE,
    @SerializedName("premium_purchase")
    PREMIUM_PURCHASE,
    @SerializedName("business_account_transfer")
    BUSINESS_ACCOUNT_TRANSFER,
}
/** Describes a transaction with a user. */
class TgTransactionPartnerUser(
    /** Type of the transaction, currently one of “invoice_payment” for payments via invoices, “paid_media_payment” for payments for paid media, “gift_purchase” for gifts sent by the bot, “premium_purchase” for Telegram Premium subscriptions gifted by the bot, “business_account_transfer” for direct transfers from managed business accounts */
    @SerializedName("transaction_type")
    val transactionType: TgTransactionType,
    /** Information about the user */
    val user: TgUser,
    /** Information about the affiliate that received a commission via this transaction. Can be available only for “invoice_payment” and “paid_media_payment” transactions. */
    val affiliate: TgAffiliateInfo?,
    /** Bot-specified invoice payload. Can be available only for “invoice_payment” transactions. */
    @SerializedName("invoice_payload")
    val invoicePayload: String?,
    /** The duration of the paid subscription. Can be available only for “invoice_payment” transactions. */
    @SerializedName("subscription_period")
    val subscriptionPeriod: Int?,
    /** Information about the paid media bought by the user; for “paid_media_payment” transactions only */
    @SerializedName("paid_media")
    val paidMedia: List<TgPaidMedia>?,
    /** Bot-specified paid media payload. Can be available only for “paid_media_payment” transactions. */
    @SerializedName("paid_media_payload")
    val paidMediaPayload: String?,
    /** The gift sent to the user by the bot; for “gift_purchase” transactions only */
    val gift: TgGift?,
    /** Number of months the gifted Telegram Premium subscription will be active for; for “premium_purchase” transactions only */
    @SerializedName("premium_subscription_duration")
    val premiumSubscriptionDuration: Int?,
): TgTransactionPartner(TgTransactionPartnerType.USER)
/** Describes a transaction with a chat. */
class TgTransactionPartnerChat(
    /** Information about the chat */
    val chat: TgChat,
    /** The gift sent to the chat by the bot */
    val gift: TgGift?,
): TgTransactionPartner(TgTransactionPartnerType.CHAT)
/** Describes the affiliate program that issued the affiliate commission received via this transaction. */
class TgTransactionPartnerAffiliateProgram(
    /** Information about the bot that sponsored the affiliate program */
    @SerializedName("sponsor_user")
    val sponsorUser: TgUser?,
    /** The number of Telegram Stars received by the bot for each 1000 Telegram Stars received by the affiliate program sponsor from referred users */
    @SerializedName("commission_per_mille")
    val commissionPerMille: Int,
): TgTransactionPartner(TgTransactionPartnerType.AFFILIATE_PROGRAM)
/** Describes a withdrawal transaction with Fragment. */
class TgTransactionPartnerFragment(
    /** State of the transaction if the transaction is outgoing */
    @SerializedName("withdrawal_state")
    val withdrawalState: TgRevenueWithdrawalState,
): TgTransactionPartner(TgTransactionPartnerType.FRAGMENT)
/** Describes a withdrawal transaction with Fragment. */
class TgTransactionPartnerTelegramAds: TgTransactionPartner(TgTransactionPartnerType.TELEGRAM_ADS)
/** Describes a transaction with payment for paid broadcasting. */
class TgTransactionPartnerTelegramApi(
    /** The number of successful requests that exceeded regular limits and were therefore billed */
    @SerializedName("request_count")
    val requestCount: Int,
): TgTransactionPartner(TgTransactionPartnerType.TELEGRAM_ADS)
/** Describes a transaction with an unknown source or recipient. */
class TgTransactionPartnerOther: TgTransactionPartner(TgTransactionPartnerType.OTHER)

/** Describes a Telegram Star transaction. Note that if the buyer initiates a chargeback with the payment provider from whom they acquired Stars (e.g., Apple, Google) following this transaction, the refunded Stars will be deducted from the bot's balance. This is outside of Telegram's control. */
data class TgStarTransaction(
    /** Unique identifier of the transaction. Coincides with the identifier of the original transaction for refund transactions. Coincides with SuccessfulPayment.telegram_payment_charge_id for successful incoming payments from users. */
    val id: String,
    /** Integer amount of Telegram Stars transferred by the transaction */
    val amount: Int,
    /** The number of 1/1000000000 shares of Telegram Stars transferred by the transaction; from 0 to 999999999 */
    @SerializedName("nanostar_amount")
    val nanostarAmount: Int?,
    /** Date the transaction was created in Unix time */
    val date: Int,
    /** Source of an incoming transaction (e.g., a user purchasing goods or services, Fragment refunding a failed withdrawal). Only for incoming transactions */
    val source: TgTransactionPartner?,
    /** Receiver of an outgoing transaction (e.g., a user for a purchase refund, Fragment for a withdrawal). Only for outgoing transactions */
    val receiver: TgTransactionPartner?,
)
/** Contains a list of Telegram Star transactions. */
data class TgStarTransactions(
    /** The list of transactions */
    val transactions: List<TgStarTransaction>
)