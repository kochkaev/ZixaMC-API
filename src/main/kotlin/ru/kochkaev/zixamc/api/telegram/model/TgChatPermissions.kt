package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** Describes actions that a non-administrator user is allowed to take in a chat. */
open class TgChatPermissions(
    /** True, if the user is allowed to send text messages, contacts, giveaways, giveaway winners, invoices, locations and venues */
    @SerializedName("can_send_messages")
    val canSendMessages: Boolean?,
    /** True, if the user is allowed to send audios */
    @SerializedName("can_send_audios")
    val canSendAudios: Boolean?,
    /** True, if the user is allowed to send documents */
    @SerializedName("can_send_documents")
    val canSendDocuments: Boolean?,
    /** True, if the user is allowed to send photos */
    @SerializedName("can_send_photos")
    val canSendPhotos: Boolean?,
    /** True, if the user is allowed to send videos */
    @SerializedName("can_send_videos")
    val canSendVideos: Boolean?,
    /** True, if the user is allowed to send video notes */
    @SerializedName("can_send_video_notes")
    val canSendVideoNotes: Boolean?,
    /** True, if the user is allowed to send voice notes */
    @SerializedName("can_send_voice_notes")
    val canSendVoiceNotes: Boolean?,
    /** True, if the user is allowed to send polls and checklists */
    @SerializedName("can_send_polls")
    val canSendPolls: Boolean?,
    /** True, if the user is allowed to send animations, games, stickers and use inline bots */
    @SerializedName("can_send_other_messages")
    val canSendOtherMessages: Boolean?,
    /** True, if the user is allowed to add web page previews to their messages */
    @SerializedName("can_add_web_page_previews")
    val canAddWebPagePreviews: Boolean?,
    /** True, if the user is allowed to change the chat title, photo and other settings. Ignored in public supergroups */
    @SerializedName("can_change_info")
    val canChangeInfo: Boolean?,
    /** True, if the user is allowed to invite new users to the chat */
    @SerializedName("can_invite_users")
    val canInviteUsers: Boolean?,
    /** True, if the user is allowed to pin messages. Ignored in public supergroups */
    @SerializedName("can_pin_messages")
    val canPinMessages: Boolean?,
    /** True, if the user is allowed to create forum topics. If omitted defaults to the value of can_pin_messages */
    @SerializedName("can_manage_topics")
    val canManageTopics: Boolean?,
)
