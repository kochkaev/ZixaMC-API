package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.serialize.TgChatMemberAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTypeEnum

/** This object contains information about one member of a chat. Currently, the following 6 types of chat members are supported: ChatMemberOwner, ChatMemberAdministrator, ChatMemberMember, ChatMemberRestricted, ChatMemberLeft or ChatMemberBanned */
@JsonAdapter(TgChatMemberAdapter::class)
open class TgChatMember(
    /** The member's status in the chat */
    val status: TgChatMemberStatuses,
    /** Information about the user */
    val user: TgUser
)
enum class TgChatMemberStatuses: TgTypeEnum {
    @SerializedName("creator")
    CREATOR {
        override val model = TgChatMemberOwner::class.java
    },
    @SerializedName("administrator")
    ADMINISTRATOR {
        override val model = TgChatMemberAdministrator::class.java
    },
    @SerializedName("member")
    MEMBER {
        override val model = TgChatMemberMember::class.java
    },
    @SerializedName("restricted")
    RESTRICTED {
        override val model = TgChatMemberRestricted::class.java
    },
    @SerializedName("left")
    LEFT {
        override val model = TgChatMemberLeft::class.java
    },
    @SerializedName("kicked")
    KICKED {
        override val model = TgChatMemberBanned::class.java
    },
}

/** Represents a chat member that owns the chat and has all administrator privileges. */
class TgChatMemberOwner(
    user: TgUser,
    /** True, if the user's presence in the chat is hidden */
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean = false,
    /** Custom title for this user */
    @SerializedName("custom_title")
    val customTitle: String? = null
): TgChatMember(TgChatMemberStatuses.CREATOR, user)
/** Represents a chat member that has some additional privileges. */
class TgChatMemberAdministrator(
    user: TgUser,
    /** True, if the bot is allowed to edit administrator privileges of that user */
    @SerializedName("can_be_edited")
    val canBeEdited: Boolean = false,
    /**True, if the user's presence in the chat is hidden  */
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean = false,
    /** True, if the administrator can access the chat event log, get boost list, see hidden supergroup and channel members, report spam messages, ignore slow mode, and send messages to the chat without paying Telegram Stars. Implied by any other administrator privilege. */
    @SerializedName("can_manage_chat")
    val canManageChat: Boolean = false,
    /** True, if the administrator can delete messages of other users */
    @SerializedName("can_delete_messages")
    val canDeleteMessages: Boolean = false,
    /** True, if the administrator can manage video chats */
    @SerializedName("can_manage_video_chats")
    val canManageVideoChats: Boolean = false,
    /** True, if the administrator can restrict, ban or unban chat members, or access supergroup statistics */
    @SerializedName("can_restrict_members")
    val canRestrictMembers: Boolean = false,
    /** True, if the administrator can add new administrators with a subset of their own privileges or demote administrators that they have promoted, directly or indirectly (promoted by administrators that were appointed by the user) */
    @SerializedName("can_promote_members")
    val canPromoteMembers: Boolean = false,
    /** True, if the user is allowed to change the chat title, photo and other settings */
    @SerializedName("can_change_info")
    val canChangeInfo: Boolean = false,
    /** True, if the user is allowed to invite new users to the chat */
    @SerializedName("can_invite_users")
    val canInviteUsers: Boolean = false,
    /** True, if the administrator can post stories to the chat */
    @SerializedName("can_post_stories")
    val canPostStories: Boolean = false,
    /** True, if the administrator can edit stories posted by other users, post stories to the chat page, pin chat stories, and access the chat's story archive */
    @SerializedName("can_edit_stories")
    val canEditStories: Boolean = false,
    /** True, if the administrator can delete stories posted by other users */
    @SerializedName("can_delete_stories")
    val canDeleteStories: Boolean = false,
    /** True, if the administrator can post messages in the channel, approve suggested posts, or access channel statistics; for channels only */
    @SerializedName("can_post_messages")
    val canPostMessages: Boolean? = null,
    /** True, if the administrator can edit messages of other users and can pin messages; for channels only */
    @SerializedName("can_edit_messages")
    val canEditMessages: Boolean? = null,
    /** True, if the user is allowed to pin messages; for groups and supergroups only */
    @SerializedName("can_pin_messages")
    val canPinMessages: Boolean? = null,
    /** True, if the user is allowed to create, rename, close, and reopen forum topics; for supergroups only */
    @SerializedName("can_manage_topics")
    val canManageTopics: Boolean? = null,
    /** Custom title for this user */
    @SerializedName("custom_title")
    val customTitle: String? = null,
): TgChatMember(TgChatMemberStatuses.ADMINISTRATOR, user)
/** Represents a chat member that has no additional privileges or restrictions. */
class TgChatMemberMember(
    user: TgUser,
    /** Date when the user's subscription will expire; Unix time */
    @SerializedName("until_date")
    val untilDate: Int? = null,
): TgChatMember(TgChatMemberStatuses.MEMBER, user)
/** Represents a chat member that is under certain restrictions in the chat. Supergroups only. */
class TgChatMemberRestricted(
    user: TgUser,
    /** True, if the user is a member of the chat at the moment of the request */
    @SerializedName("is_member")
    val isMember: Boolean = true,
    /** True, if the user is allowed to send text messages, contacts, giveaways, giveaway winners, invoices, locations and venues */
    @SerializedName("can_send_messages")
    val canSendMessages: Boolean,
    /** True, if the user is allowed to send audios */
    @SerializedName("can_send_audios")
    val canSendAudios: Boolean,
    /** True, if the user is allowed to send documents */
    @SerializedName("can_send_documents")
    val canSendDocuments: Boolean,
    /** True, if the user is allowed to send photos */
    @SerializedName("can_send_photos")
    val canSendPhotos: Boolean,
    /** True, if the user is allowed to send videos */
    @SerializedName("can_send_videos")
    val canSendVideos: Boolean,
    /** True, if the user is allowed to send video notes */
    @SerializedName("can_send_video_notes")
    val canSendVideoNotes: Boolean,
    /** True, if the user is allowed to send voice notes */
    @SerializedName("can_send_voice_notes")
    val canSendVoiceNotes: Boolean,
    /** True, if the user is allowed to send polls and checklists */
    @SerializedName("can_send_polls")
    val canSendPolls: Boolean,
    /** True, if the user is allowed to send animations, games, stickers and use inline bots */
    @SerializedName("can_send_other_messages")
    val canSendOtherMessages: Boolean,
    /** True, if the user is allowed to add web page previews to their messages */
    @SerializedName("can_add_web_page_previews")
    val canAddWebPagePreviews: Boolean,
    /** True, if the user is allowed to change the chat title, photo and other settings. Ignored in public supergroups */
    @SerializedName("can_change_info")
    val canChangeInfo: Boolean,
    /** True, if the user is allowed to invite new users to the chat */
    @SerializedName("can_invite_users")
    val canInviteUsers: Boolean,
    /** True, if the user is allowed to pin messages. Ignored in public supergroups */
    @SerializedName("can_pin_messages")
    val canPinMessages: Boolean,
    /** True, if the user is allowed to create forum topics. If omitted defaults to the value of can_pin_messages */
    @SerializedName("can_manage_topics")
    val canManageTopics: Boolean,
    /** Date when restrictions will be lifted for this user; Unix time. If 0, then the user is restricted forever */
    @SerializedName("until_date")
    val untilDate: Int? = null,
): TgChatMember(TgChatMemberStatuses.RESTRICTED, user)
/** Represents a chat member that isn't currently a member of the chat, but may join it themselves. */
class TgChatMemberLeft(
    user: TgUser
): TgChatMember(TgChatMemberStatuses.LEFT, user)
/** Represents a chat member that was banned in the chat and can't return to the chat or view chat messages. */
class TgChatMemberBanned(
    user: TgUser,
    /** Date when restrictions will be lifted for this user; Unix time. If 0, then the user is banned forever */
    @SerializedName("until_date")
    val untilDate: Int
): TgChatMember(TgChatMemberStatuses.KICKED, user)
