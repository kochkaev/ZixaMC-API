package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object represents an incoming update. At most one of the optional parameters can be present in any given update. */
data class TgUpdate(
    /** The update's unique identifier. Update identifiers start from a certain positive number and increase sequentially. This identifier becomes especially handy if you're using webhooks, since it allows you to ignore repeated updates or to restore the correct update sequence, should they get out of order. If there are no new updates for at least a week, then identifier of the next update will be chosen randomly instead of sequentially. */
    @SerializedName("update_id")
    val updateId: Int,
    /** New incoming message of any kind - text, photo, sticker, etc. */
    val message: TgMessage? = null,
    /** New version of a message that is known to the bot and was edited. This update may at times be triggered by changes to message fields that are either unavailable or not actively used by your bot. */
    @SerializedName("edited_message")
    val editedMessage: TgMessage? = null,
    /** New incoming channel post of any kind - text, photo, sticker, etc. */
    @SerializedName("channel_post")
    val channelPost: TgMessage? = null,
    /** New version of a channel post that is known to the bot and was edited. This update may at times be triggered by changes to message fields that are either unavailable or not actively used by your bot. */
    @SerializedName("edited_channel_post")
    val editedChannelPost: TgMessage? = null,
    /** The bot was connected to or disconnected from a business account, or a user edited an existing connection with the bot*/
    @SerializedName("business_connection")
    val businessConnection: TgBusinessConnection? = null,
    /** New message from a connected business account */
    @SerializedName("business_message")
    val businessMessage: TgMessage? = null,
    /** New version of a message from a connected business account */
    @SerializedName("edited_business_message")
    val editedBusinessMessage: TgMessage? = null,
    /** Messages were deleted from a connected business account */
    @SerializedName("deleted_business_messages")
    val deletedBusinessMessages: TgBusinessMessagesDeleted? = null,
    /** A reaction to a message was changed by a user. The bot must be an administrator in the chat and must explicitly specify "message_reaction" in the list of allowed_updates to receive these updates. The update isn't received for reactions set by bots. */
    @SerializedName("message_reaction")
    val messageReaction: TgMessageReactionUpdated? = null,
    /** Reactions to a message with anonymous reactions were changed. The bot must be an administrator in the chat and must explicitly specify "message_reaction_count" in the list of allowed_updates to receive these updates. The updates are grouped and can be sent with delay up to a few minutes. */
    @SerializedName("message_reaction_count")
    val messageReactionCount: TgMessageReactionCountUpdated? = null,
    /** New incoming inline query */
    @SerializedName("inline_query")
    val inlineQuery: TgInlineQuery? = null,
    /** The result of an inline query that was chosen by a user and sent to their chat partner. Please see our documentation on the feedback collecting for details on how to enable these updates for your bot. */
    @SerializedName("chosen_inline_result")
    val chosenInlineResult: TgChosenInlineResult? = null,
    /** New incoming callback query */
    @SerializedName("callback_query")
    val callbackQuery: TgCallbackQuery? = null,
    /** New incoming shipping query. Only for invoices with flexible price */
    @SerializedName("shipping_query")
    val shippingQuery: TgShippingQuery? = null,
    /** New incoming pre-checkout query. Contains full information about checkout */
    @SerializedName("pre_checkout_query")
    val preCheckoutQuery: TgPreCheckoutQuery? = null,
    /** A user purchased paid media with a non-empty payload sent by the bot in a non-channel chat */
    @SerializedName("purchased_paid_media")
    val purchasedPaidMedia: TgPaidMediaPurchased? = null,
    /** New poll state. Bots receive only updates about manually stopped polls and polls, which are sent by the bot */
    val poll: TgPoll? = null,
    /** A user changed their answer in a non-anonymous poll. Bots receive new votes only in polls that were sent by the bot itself. */
    @SerializedName("poll_answer")
    val pollAnswer: TgPollAnswer? = null,
    /** The bot's chat member status was updated in a chat. For private chats, this update is received only when the bot is blocked or unblocked by the user. */
    @SerializedName("my_chat_member")
    val myChatMember: TgChatMemberUpdated? = null,
    /** A chat member's status was updated in a chat. The bot must be an administrator in the chat and must explicitly specify "chat_member" in the list of allowed_updates to receive these updates. */
    @SerializedName("chat_member")
    val chatMember: TgChatMemberUpdated? = null,
    /** A request to join the chat has been sent. The bot must have the can_invite_users administrator right in the chat to receive these updates. */
    @SerializedName("chat_join_request")
    val chatJoinRequest: TgChatJoinRequest? = null,
    /** A chat boost was added or changed. The bot must be an administrator in the chat to receive these updates. */
    @SerializedName("chat_boost")
    val chatBoost: TgChatBoostUpdated? = null,
    /** A boost was removed from a chat. The bot must be an administrator in the chat to receive these updates. */
    @SerializedName("removed_chat_boost")
    val removedChatBoost: TgChatBoostRemoved? = null,
)
