package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

open class TgReplyMarkup: ITgMenu

/** This object represents an inline keyboard that appears right next to the message it belongs to. */
data class TgInlineKeyboardMarkup(
    /** Array of button rows, each represented by an Array of InlineKeyboardButton objects */
    @SerializedName("inline_keyboard")
    val inlineKeyboard: List<List<TgInlineKeyboardButton>>
) : TgReplyMarkup()
/** This object represents one button of an inline keyboard. Exactly one of the optional fields must be used to specify type of the button. */
data class TgInlineKeyboardButton(
    /** Label text on the button */
    val text: String,
    /** HTTP or tg:// URL to be opened when the button is pressed. Links tg://user?id=&ltuser_id&gt can be used to mention a user by their identifier without using a username, if this is allowed by their privacy settings. */
    val url: String? = null,
    /** Data to be sent in a callback query to the bot when the button is pressed, 1-64 bytes */
    @SerializedName("callback_data")
    val callbackData: String? = null,
    /** Description of the Web App that will be launched when the user presses the button. The Web App will be able to send an arbitrary message on behalf of the user using the method answerWebAppQuery. Available only in private chats between a user and the bot. Not supported for messages sent on behalf of a Telegram Business account. */
    @SerializedName("web_app")
    val webApp: TgWebAppInfo? = null,
    /** An HTTPS URL used to automatically authorize the user. Can be used as a replacement for the Telegram Login Widget. */
    @SerializedName("login_url")
    val loginUrl: TgLoginUrl? = null,
    /** If set, pressing the button will prompt the user to select one of their chats, open that chat and insert the bot's username and the specified inline query in the input field. May be empty, in which case just the bot's username will be inserted. Not supported for messages sent on behalf of a Telegram Business account. */
    @SerializedName("switch_inline_query")
    val switchInlineQuery: String? = null,
    /** If set, pressing the button will insert the bot's username and the specified inline query in the current chat's input field. May be empty, in which case only the bot's username will be inserted. This offers a quick way for the user to open your bot in inline mode in the same chat - good for selecting something from multiple options. Not supported in channels and for messages sent on behalf of a Telegram Business account. */
    @SerializedName("switch_inline_query_current_chat")
    val switchInlineQueryCurrentChat: String? = null,
    /** If set, pressing the button will insert the bot's username and the specified inline query in the current chat's input field. May be empty, in which case only the bot's username will be inserted. This offers a quick way for the user to open your bot in inline mode in the same chat - good for selecting something from multiple options. Not supported in channels and for messages sent on behalf of a Telegram Business account. */
    @SerializedName("switch_inline_query_chosen_chat")
    val switchInlineQueryChosenChat: TgSwitchInlineQueryChosenChat? = null,
    /** Description of the button that copies the specified text to the clipboard. */
    @SerializedName("copy_text")
    val copyText: TgCopyTextButton? = null,
    /** Description of the game that will be launched when the user presses the button. NOTE: This type of button must always be the first button in the first row. */
    @SerializedName("callback_game")
    val callbackGame: TgCallbackGame? = null,
    /** Optional. Specify True, to send a Pay button. Substrings “⭐” and “XTR” in the buttons's text will be replaced with a Telegram Star icon. NOTE: This type of button must always be the first button in the first row and can only be used in invoice messages. */
    val pay: Boolean? = null,
): ITgMenuButton
/** This object represents an inline keyboard button that copies specified text to the clipboard. */
data class TgCopyTextButton(
    /** The text to be copied to the clipboard; 1-256 characters */
    val text: String,
)
/** This object represents a custom keyboard with reply options (see Introduction to bots for details and examples). Not supported in channels and for messages sent on behalf of a Telegram Business account. */
data class TgReplyKeyboardMarkup(
    /** Array of button rows, each represented by an Array of KeyboardButton objects */
    val keyboard: List<List<TgKeyboardButton>>,
    /** Requests clients to always show the keyboard when the regular keyboard is hidden. Defaults to false, in which case the custom keyboard can be hidden and opened with a keyboard icon. */
    @SerializedName("is_persistent")
    val isPersistent: Boolean?,
    /** Requests clients to resize the keyboard vertically for optimal fit (e.g., make the keyboard smaller if there are just two rows of buttons). Defaults to false, in which case the custom keyboard is always of the same height as the app's standard keyboard. */
    @SerializedName("resize_keyboard")
    val resizeKeyboard: Boolean?,
    /** Requests clients to hide the keyboard as soon as it's been used. The keyboard will still be available, but clients will automatically display the usual letter-keyboard in the chat - the user can press a special button in the input field to see the custom keyboard again. Defaults to false. */
    @SerializedName("one_time_keyboard")
    val oneTimeKeyboard: Boolean?,
    /** The placeholder to be shown in the input field when the keyboard is active; 1-64 characters */
    @SerializedName("input_field_placeholder")
    val inputFieldPlaceholder: String?,
    /** Use this parameter if you want to show the keyboard to specific users only. Targets: 1) users that are @mentioned in the text of the Message object; 2) if the bot's message is a reply to a message in the same chat and forum topic, sender of the original message. Example: A user requests to change the bot's language, bot replies to the request with a keyboard to select the new language. Other users in the group don't see the keyboard. */
    val selective: Boolean?,
) : TgReplyMarkup()
/** This object represents one button of the reply keyboard. At most one of the optional fields must be used to specify type of the button. For simple text buttons, String can be used instead of this object to specify the button text. */
data class TgKeyboardButton(
    /** Text of the button. If none of the optional fields are used, it will be sent as a message when the button is pressed */
    val text: String,
    /** If specified, pressing the button will open a list of suitable users. Identifiers of selected users will be sent to the bot in a “users_shared” service message. Available in private chats only. */
    @SerializedName("request_users")
    val requestUsers: TgKeyboardButtonRequestUsers? = null,
    /** If specified, pressing the button will open a list of suitable chats. Tapping on a chat will send its identifier to the bot in a “chat_shared” service message. Available in private chats only. */
    @SerializedName("request_chat")
    val requestChat: TgKeyboardButtonRequestChat? = null,
    /** If True, the user's phone number will be sent as a contact when the button is pressed. Available in private chats only. */
    @SerializedName("request_contact")
    val requestContact: Boolean? = null,
    /** If True, the user's current location will be sent when the button is pressed. Available in private chats only. */
    @SerializedName("request_location")
    val requestLocation: Boolean? = null,
    /** If specified, the user will be asked to create a poll and send it to the bot when the button is pressed. Available in private chats only. */
    @SerializedName("request_poll")
    val requestPoll: TgKeyboardButtonPollType? = null,
    /** If specified, the described Web App will be launched when the button is pressed. The Web App will be able to send a “web_app_data” service message. Available in private chats only. */
    @SerializedName("web_app")
    val webApp: TgWebAppInfo? = null,
)
/** This object defines the criteria used to request suitable users. Information about the selected users will be shared with the bot when the corresponding button is pressed. More about requesting users: https://core.telegram.org/bots/features#chat-and-user-selection */
data class TgKeyboardButtonRequestUsers(
    /** Signed 32-bit identifier of the request that will be received back in the UsersShared object. Must be unique within the message */
    @SerializedName("request_id")
    val requestId: Int,
    /** Pass True to request bots, pass False to request regular users. If not specified, no additional restrictions are applied. */
    @SerializedName("user_is_bot")
    val userIsBot: Boolean? = null,
    /** Pass True to request premium users, pass False to request non-premium users. If not specified, no additional restrictions are applied. */
    @SerializedName("user_is_premium")
    val userIsPremium: Boolean? = null,
    /** The maximum number of users to be selected; 1-10. Defaults to 1. */
    @SerializedName("max_quantity")
    val maxQuantity: Int? = null,
    /** Pass True to request the users' first and last names */
    @SerializedName("request_name")
    val requestName: Boolean? = null,
    /** Pass True to request the users' usernames */
    @SerializedName("request_username")
    val requestUsername: Boolean? = null,
    /** Pass True to request the users' photos */
    @SerializedName("request_photo")
    val requestPhoto: Boolean? = null,
)
/** This object defines the criteria used to request a suitable chat. Information about the selected chat will be shared with the bot when the corresponding button is pressed. The bot will be granted requested rights in the chat if appropriate. More about requesting chats: https://core.telegram.org/bots/features#chat-and-user-selection */
data class TgKeyboardButtonRequestChat(
    /** Signed 32-bit identifier of the request, which will be received back in the ChatShared object. Must be unique within the message */
    @SerializedName("request_id")
    val requestId: Int,
    /** Pass True to request a channel chat, pass False to request a group or a supergroup chat. */
    @SerializedName("chat_is_channel")
    val chatIsChannel: Boolean,
    /** Pass True to request a forum supergroup, pass False to request a non-forum chat. If not specified, no additional restrictions are applied. */
    @SerializedName("chat_is_forum")
    val chatIsForum: Boolean? = null,
    /** Pass True to request a supergroup or a channel with a username, pass False to request a chat without a username. If not specified, no additional restrictions are applied. */
    @SerializedName("chat_has_username")
    val chatHasUsername: Boolean? = null,
    /** Pass True to request a chat owned by the user. Otherwise, no additional restrictions are applied. */
    @SerializedName("chat_is_created")
    val chatIsCreated: Boolean? = null,
    /** A JSON-serialized object listing the required administrator rights of the user in the chat. The rights must be a superset of bot_administrator_rights. If not specified, no additional restrictions are applied. */
    @SerializedName("user_administrator_rights")
    val userAdministratorRights: TgChatAdministratorRights? = null,
    /** A JSON-serialized object listing the required administrator rights of the bot in the chat. The rights must be a subset of user_administrator_rights. If not specified, no additional restrictions are applied. */
    @SerializedName("bot_administrator_rights")
    val botAdministratorRights: TgChatAdministratorRights? = null,
    /** Pass True to request a chat with the bot as a member. Otherwise, no additional restrictions are applied. */
    @SerializedName("bot_is_member")
    val botIsMember: Boolean? = null,
    /** Pass True to request the chat's title */
    @SerializedName("request_title")
    val requestTitle: Boolean? = null,
    /** Pass True to request the chat's username */
    @SerializedName("request_username")
    val requestUsername: Boolean? = null,
    /** Pass True to request the chat's photo */
    @SerializedName("request_photo")
    val requestPhoto: Boolean? = null,
)
/** This object represents type of a poll, which is allowed to be created and sent when the corresponding button is pressed. */
data class TgKeyboardButtonPollType (
    /** If quiz is passed, the user will be allowed to create only polls in the quiz mode. If regular is passed, only regular polls will be allowed. Otherwise, the user will be allowed to create a poll of any type. */
    val type: TgPollType? = null
)
/** Upon receiving a message with this object, Telegram clients will remove the current custom keyboard and display the default letter-keyboard. By default, custom keyboards are displayed until a new keyboard is sent by a bot. An exception is made for one-time keyboards that are hidden immediately after the user presses a button (see ReplyKeyboardMarkup). Not supported in channels and for messages sent on behalf of a Telegram Business account. */
data class TgReplyKeyboardRemove (
    /** Requests clients to remove the custom keyboard (user will not be able to summon this keyboard; if you want to hide the keyboard from sight but keep it accessible, use one_time_keyboard in ReplyKeyboardMarkup) */
    @SerializedName("remove_keyboard")
    val removeKeyboard: Boolean,
    /** Use this parameter if you want to remove the keyboard for specific users only. Targets: 1) users that are @mentioned in the text of the Message object; 2) if the bot's message is a reply to a message in the same chat and forum topic, sender of the original message. Example: A user votes in a poll, bot returns confirmation message in reply to the vote and removes the keyboard for that user, while still showing the keyboard with poll options to users who haven't voted yet. */
    val selective: Boolean? = null
) : TgReplyMarkup()
/**
 * Upon receiving a message with this object, Telegram clients will display a reply interface to the user (act as if the user has selected the bot's message and tapped 'Reply'). This can be extremely useful if you want to create user-friendly step-by-step interfaces without having to sacrifice privacy mode. Not supported in channels and for messages sent on behalf of a Telegram Business account.
 *
 * Example: A poll bot for groups runs in privacy mode (only receives commands, replies to its messages and mentions). There could be two ways to create a new poll:
 *
 * Explain the user how to send a command with parameters (e.g. /newpoll question answer1 answer2). May be appealing for hardcore users but lacks modern day polish.
 * Guide the user through a step-by-step process. 'Please send me your question', 'Cool, now let's add the first answer option', 'Great. Keep adding answer options, then send /done when you're ready'.
 *
 * The last option is definitely more attractive. And if you use ForceReply in your bot's questions, it will receive the user's answers even if it only receives replies, commands and mentions - without any extra work for the user.
 */
data class TgForceReply (
    /** Shows reply interface to the user, as if they manually selected the bot's message and tapped 'Reply' */
    @SerializedName("force_reply")
    val forceReply: Boolean,
    /** The placeholder to be shown in the input field when the reply is active; 1-64 characters */
    @SerializedName("input_field_placeholder")
    val inputFieldPlaceholder: String? = null,
    /** Use this parameter if you want to force reply from specific users only. Targets: 1) users that are @mentioned in the text of the Message object; 2) if the bot's message is a reply to a message in the same chat and forum topic, sender of the original message. */
    val selective: Boolean? = null
) : TgReplyMarkup()
/** This object represents a parameter of the inline keyboard button used to automatically authorize a user. Serves as a great replacement for the Telegram Login Widget when the user is coming from Telegram. All the user needs to do is tap/click a button and confirm that they want to log in. Telegram apps support these buttons as of version 5.7. */
data class TgLoginUrl(
    /** An HTTPS URL to be opened with user authorization data added to the query string when the button is pressed. If the user refuses to provide authorization data, the original URL without information about the user will be opened. The data added is the same as described in Receiving authorization data. */
    val url: String,
    /** New text of the button in forwarded messages. */
    @SerializedName("forward_text")
    val forwardText: String? = null,
    /** Username of a bot, which will be used for user authorization. See Setting up a bot for more details. If not specified, the current bot's username will be assumed. The url's domain must be the same as the domain linked with the bot. See Linking your domain to the bot for more details: https://core.telegram.org/widgets/login#linking-your-domain-to-the-bot */
    @SerializedName("request_write_access")
    val requestWriteAccess: Boolean? = null,
)
/** This object represents an inline button that switches the current user to inline mode in a chosen chat, with an optional default inline query. */
data class TgSwitchInlineQueryChosenChat(
    /** The default inline query to be inserted in the input field. If left empty, only the bot's username will be inserted */
    val query: String,
    /** True, if private chats with users can be chosen */
    @SerializedName("allow_user_chats")
    val allowUserChats: Boolean? = null,
    /** True, if private chats with bots can be chosen */
    @SerializedName("allow_bot_chats")
    val allowBotChats: Boolean? = null,
    /** True, if group and supergroup chats can be chosen */
    @SerializedName("allow_group_chats")
    val allowGroupChats: Boolean? = null,
    /** True, if channel chats can be chosen */
    @SerializedName("allow_channel_chats")
    val allowChannelChats: Boolean? = null,
)