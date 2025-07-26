package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.serialize.TgMaybeInaccessibleMessageAdapter

/** This object represents a message. */
class TgMessage(
    /** Unique message identifier inside this chat. In specific instances (e.g., message containing a video sent to a big chat), the server might automatically schedule a message instead of sending it immediately. In such cases, this field will be 0 and the relevant message will be unusable until it is actually sent */
    messageId: Int,
    /** Unique identifier of a message thread to which the message belongs; for supergroups only */
    @SerializedName("message_thread_id")
    val messageThreadId: Int? = null,
    /** Sender of the message; may be empty for messages sent to channels. For backward compatibility, if the message was sent on behalf of a chat, the field contains a fake sender user in non-channel chats */
    val from: TgUser? = null,
    /** Sender of the message when sent on behalf of a chat. For example, the supergroup itself for messages sent by its anonymous administrators or a linked channel for messages automatically forwarded to the channel's discussion group. For backward compatibility, if the message was sent on behalf of a chat, the field from contains a fake sender user in non-channel chats. */
    @SerializedName("sender_chat")
    val senderChat: TgChat? = null,
    /** If the sender of the message boosted the chat, the number of boosts added by the user */
    @SerializedName("sender_boost_count")
    val senderBoostCount: Int? = null,
    /** The bot that actually sent the message on behalf of the business account. Available only for outgoing messages sent on behalf of the connected business account. */
    @SerializedName("sender_business_bot")
    val senderBusinessBot: TgUser? = null,
    /** Date the message was sent in Unix time. It is always a positive number, representing a valid date. */
    date: Int,
    /** Unique identifier of the business connection from which the message was received. If non-empty, the message belongs to a chat of the corresponding business account that is independent from any potential bot chat which might share the same identifier. */
    @SerializedName("business_connection_id")
    val businessConnectionId: String,
    /** Chat the message belongs to */
    chat: TgChat,
    /** Information about the original message for forwarded messages */
    @SerializedName("forward_origin")
    val forwardOrigin: TgMessageOrigin? = null,
    /** True, if the message is sent to a forum topic */
    @SerializedName("is_topic_message")
    val isTopicMessage: Boolean? = null,
    /** True, if the message is a channel post that was automatically forwarded to the connected discussion group */
    @SerializedName("is_automatic_forward")
    val isAutomaticForward: Boolean? = null,
    /** For replies in the same chat and message thread, the original message. Note that the Message object in this field will not contain further reply_to_message fields even if it itself is a reply. */
    @SerializedName("reply_to_message")
    val replyToMessage: TgMessage? = null,
    /** Information about the message that is being replied to, which may come from another chat or forum topic */
    @SerializedName("external_reply")
    val externalReply: TgExternalReplyInfo? = null,
    /** For replies that quote part of the original message, the quoted part of the message */
    val quote: TgTextQuote? = null,
    /** For replies to a story, the original story */
    @SerializedName("reply_to_story")
    val replyToStory: TgStory? = null,
    /** Bot through which the message was sent */
    @SerializedName("via_bot")
    val viaBot: TgUser? = null,
    /** Date the message was last edited in Unix time */
    @SerializedName("edit_date")
    val editDate: Int? = null,
    /** True, if the message can't be forwarded */
    @SerializedName("has_protected_content")
    val hasProtectedContent: Boolean? = null,
    /** True, if the message was sent by an implicit action, for example, as an away or a greeting business message, or as a scheduled message */
    @SerializedName("is_from_offline")
    val isFromOffline: Boolean? = null,
    /** The unique identifier of a media message group this message belongs to */
    @SerializedName("media_group_id")
    val mediaGroupId: String? = null,
    /** Signature of the post author for messages in channels, or the custom title of an anonymous group administrator */
    @SerializedName("author_signature")
    val authorSignature: String? = null,
    /** The number of Telegram Stars that were paid by the sender of the message to send it */
    @SerializedName("paid_star_count")
    val paidStarCount: Int? = null,
    /** For text messages, the actual UTF-8 text of the message */
    val text: String? = null,
    /** For text messages, special entities like usernames, URLs, bot commands, etc. that appear in the text */
    val entities: List<TgEntity>? = null,
    /** Options used for link preview generation for the message, if it is a text message and link preview options were changed */
    @SerializedName("link_preview_options")
    val linkPreviewOptions: TgLinkPreviewOptions? = null,
    /** Unique identifier of the message effect added to the message */
    @SerializedName("effect_id")
    val effectId: String? = null,
    /** Message is an animation, information about the animation. For backward compatibility, when this field is set, the document field will also be set */
    val animation: TgAnimation? = null,
    /** Message is an animation, information about the animation. For backward compatibility, when this field is set, the document field will also be set */
    val audio: TgAudio? = null,
    /** Message is a general file, information about the file */
    val document: TgDocument? = null,
    /** Message contains paid media; information about the paid media */
    @SerializedName("paid_media")
    val paidMedia: TgPaidMediaInfo? = null,
    /** Message is a photo, available sizes of the photo */
    val photo: List<TgPhotoSize>? = null,
    /** Message is a sticker, information about the sticker */
    val sticker: TgSticker? = null,
    /** Message is a forwarded story */
    val story: TgStory? = null,
    /** Message is a video, information about the video */
    val video: TgVideo? = null,
    /** Message is a video note, information about the video message */
    @SerializedName("video_note")
    val videoNote: TgVideoNote? = null,
    /** Message is a voice message, information about the file */
    val voice: TgVoice? = null,
    /** Caption for the animation, audio, document, paid media, photo, video or voice */
    val caption: String? = null,
    /** For messages with a caption, special entities like usernames, URLs, bot commands, etc. that appear in the caption */
    @SerializedName("caption_entities")
    val captionEntities: List<TgEntity>? = null,
    /** True, if the caption must be shown above the message media */
    @SerializedName("show_caption_above_media")
    val showCaptionAboveMedia: Boolean? = null,
    /** True, if the message media is covered by a spoiler animation */
    @SerializedName("has_media_spoiler")
    val hasMediaSpoiler: Boolean? = null,
    /** Message is a checklist */
    val checklist: TgChecklist? = null,
    /** Message is a shared contact, information about the contact */
    val contact: TgContact? = null,
    /** Message is a dice with random value */
    val dice: TgDice? = null,
    /** Message is a game, information about the game. More about games: https://core.telegram.org/bots/api#games */
    val game: TgGame? = null,
    /** Message is a native poll, information about the poll */
    val poll: TgPoll? = null,
    /** Message is a venue, information about the venue. For backward compatibility, when this field is set, the location field will also be set */
    val venue: TgVenue? = null,
    /** Message is a shared location, information about the location */
    val location: TgLocation? = null,
    /** New members that were added to the group or supergroup and information about them (the bot itself may be one of these members) */
    @SerializedName("new_chat_members")
    val newChatMembers: List<TgUser>? = null,
    /** A member was removed from the group, information about them (this member may be the bot itself) */
    @SerializedName("left_chat_member")
    val leftChatMember: TgUser? = null,
    /** A chat title was changed to this value */
    @SerializedName("new_chat_title")
    val newChatTitle: String? = null,
    /** A chat title was changed to this value */
    @SerializedName("new_chat_photo")
    val newChatPhoto: List<TgPhotoSize>? = null,
    /** Service message: the chat photo was deleted */
    @SerializedName("delete_chat_photo")
    val deleteChatPhoto: Boolean? = null,
    /** Service message: the group has been created */
    @SerializedName("group_chat_created")
    val groupChatCreated: Boolean? = null,
    /** Service message: the supergroup has been created. This field can't be received in a message coming through updates, because bot can't be a member of a supergroup when it is created. It can only be found in reply_to_message if someone replies to a very first message in a directly created supergroup. */
    @SerializedName("supergroup_chat_created")
    val supergroupChatCreated: Boolean? = null,
    /** Service message: the channel has been created. This field can't be received in a message coming through updates, because bot can't be a member of a channel when it is created. It can only be found in reply_to_message if someone replies to a very first message in a channel. */
    @SerializedName("channel_chat_created")
    val channelChatCreated: Boolean? = null,
    /** Service message: auto-delete timer settings changed in the chat */
    @SerializedName("message_auto_delete_timer_changed")
    val messageAutoDeleteTimerChanged: TgMessageAutoDeleteTimerChanged? = null,
    @SerializedName("migrate_to_chat_id")
    /** The group has been migrated to a supergroup with the specified identifier. This number may have more than 32 significant bits and some programming languages may have difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so a signed 64-bit integer or double-precision float type are safe for storing this identifier. */
    val migrateToChatId: Long? = null,
    /** The supergroup has been migrated from a group with the specified identifier. This number may have more than 32 significant bits and some programming languages may have difficulty/silent defects in interpreting it. But it has at most 52 significant bits, so a signed 64-bit integer or double-precision float type are safe for storing this identifier. */
    @SerializedName("migrate_from_chat_id")
    val migrateFromChatId: Long? = null,
    /** Specified message was pinned. Note that the Message object in this field will not contain further reply_to_message fields even if it itself is a reply. */
    @SerializedName("pinned_message")
    val pinnedMessage: TgMaybeInaccessibleMessage? = null,
    /** Message is an invoice for a payment, information about the invoice. More about payments: https://core.telegram.org/bots/api#payments */
    val invoice: TgInvoice? = null,
    /** Message is a service message about a successful payment, information about the payment. More about payments: https://core.telegram.org/bots/api#payments */
    @SerializedName("successful_payment")
    val successfulPayment: TgSuccessfulPayment? = null,
    /** Message is a service message about a refunded payment, information about the payment. More about payments: https://core.telegram.org/bots/api#payments */
    @SerializedName("refunded_payment")
    val refundedPayment: TgRefundedPayment? = null,
    /** Service message: users were shared with the bot */
    @SerializedName("users_shared")
    val usersShared: TgUsersShared? = null,
    /** Service message: a chat was shared with the bot */
    @SerializedName("chat_shared")
    val chatShared: TgChatShared? = null,
    /** Service message: a regular gift was sent or received */
    @SerializedName("chat_shared")
    val gift: TgGiftInfo? = null,
    /** Service message: a unique gift was sent or received */
    @SerializedName("unique_gift")
    val uniqueGift: TgUniqueGiftInfo? = null,
    /** The domain name of the website on which the user has logged in. More about Telegram Login: https://core.telegram.org/widgets/login */
    @SerializedName("connected_website")
    val connectedWebsite: String? = null,
    /** Service message: the user allowed the bot to write messages after adding it to the attachment or side menu, launching a Web App from a link, or accepting an explicit request from a Web App sent by the method requestWriteAccess */
    @SerializedName("write_access_allowed")
    val writeAccessAllowed: TgWriteAccessAllowed? = null,
    /** Telegram Passport data */
    @SerializedName("passport_data")
    val passportData: TgPassportData? = null,
    /** Service message. A user in the chat triggered another user's proximity alert while sharing Live Location. */
    @SerializedName("proximity_alert_triggered")
    val proximityAlertTriggered: TgProximityAlertTriggered? = null,
    /** Service message: user boosted the chat */
    @SerializedName("boost_added")
    val boostAdded: TgChatBoostAdded? = null,
    /** Service message: chat background set */
    @SerializedName("chat_background_set")
    val chatBackgroundSet: TgChatBackground? = null,
    /** Service message: some tasks in a checklist were marked as done or not done */
    @SerializedName("checklist_tasks_done")
    val checklistTasksDone: TgChecklistTasksDone? = null,
    /** Service message: tasks were added to a checklist */
    @SerializedName("checklist_tasks_added")
    val checklistTasksAdded: TgChecklistTasksAdded? = null,
    /** Service message: the price for paid messages in the corresponding direct messages chat of a channel has changed */
    @SerializedName("direct_message_price_changed")
    val directMessagePriceChanged: TgDirectMessagePriceChanged? = null,
    /** Service message: forum topic created */
    @SerializedName("forum_topic_created")
    val forumTopicCreated: TgForumTopicCreated? = null,
    /** Service message: forum topic edited */
    @SerializedName("forum_topic_edited")
    val forumTopicEdited: TgForumTopicEdited? = null,
    /** Service message: forum topic closed */
    @SerializedName("forum_topic_closed")
    val forumTopicClosed: TgForumTopicClosed? = null,
    /** Service message: forum topic reopened */
    @SerializedName("forum_topic_reopened")
    val forumTopicReopened: TgForumTopicReopened? = null,
    /** Service message: the 'General' forum topic hidden */
    @SerializedName("general_forum_topic_hidden")
    val generalForumTopicHidden: TgGeneralForumTopicHidden? = null,
    /** Service message: the 'General' forum topic unhidden */
    @SerializedName("general_forum_topic_unhidden")
    val generalForumTopicUnhidden: TgGeneralForumTopicUnhidden? = null,
    /** Service message: a scheduled giveaway was created */
    @SerializedName("giveaway_created")
    val giveawayCreated: TgGiveawayCreated? = null,
    /** The message is a scheduled giveaway message */
    val giveaway: TgGiveaway? = null,
    /** A giveaway with public winners was completed */
    @SerializedName("giveaway_winners")
    val giveawayWinners: TgGiveawayWinners? = null,
    /** Service message: a giveaway without public winners was completed */
    @SerializedName("giveaway_completed")
    val giveawayCompleted: TgGiveawayCompleted? = null,
    /** Service message: the price for paid messages has changed in the chat */
    @SerializedName("paid_message_price_changed")
    val paidMessagePriceChanged: TgPaidMessagePriceChanged? = null,
    /** Service message: video chat scheduled */
    @SerializedName("video_chat_scheduled")
    val videoChatScheduled: TgVideoChatScheduled? = null,
    /** Service message: video chat started */
    @SerializedName("video_chat_started")
    val videoChatStarted: TgVideoChatStarted? = null,
    /** Service message: video chat ended */
    @SerializedName("video_chat_ended")
    val videoChatEnded: TgVideoChatEnded? = null,
    /** Service message: new participants invited to a video chat */
    @SerializedName("video_chat_participants_invited")
    val videoChatParticipantsInvited: TgVideoChatParticipantsInvited? = null,
    /** Service message: data sent by a Web App */
    @SerializedName("web_app_data")
    val webAppData: TgWebAppData? = null,
    /** Inline keyboard attached to the message. login_url buttons are represented as ordinary url buttons. */
    @SerializedName("web_app_data")
    val replyMarkup: TgInlineKeyboardMarkup? = null,
//    @SerializedName("forward_from")
//    val forwardFrom: TgUser? = null,
//    @SerializedName("forward_from_chat")
//    val forwardFromChat: TgChat? = null,
): TgMaybeInaccessibleMessage(chat, messageId, date) {
    val senderName
        get() = authorSignature
            ?: senderChat?.title
            ?: from?.let {
                (it.firstName + " " + (it.lastName ?: "")).trim()
            }
            ?: ""
    val senderUserName
        get() = from?.username
            ?: senderChat?.username
            ?: ""
    val effectiveText
        get() = text ?: caption
}

/** This object describes a message that can be inaccessible to the bot. It can be one of Message or InaccessibleMessage */
@JsonAdapter(TgMaybeInaccessibleMessageAdapter::class)
open class TgMaybeInaccessibleMessage(
    val chat: TgChat,
    @SerializedName("message_id")
    val messageId: Int,
    val date: Int = 0,
) {
    val orNull: TgMessage?
        get() = this as? TgMessage
}

/** This object describes a message that was deleted or is otherwise inaccessible to the bot. */
class TgInaccessibleMessage(
    /** Chat the message belonged to */
    chat: TgChat,
    /** Unique message identifier inside the chat */
    messageId: Int,
    /** Always 0. The field can be used to differentiate regular and inaccessible messages. */
    date: Int = 0,
): TgMaybeInaccessibleMessage(chat, messageId, date)
