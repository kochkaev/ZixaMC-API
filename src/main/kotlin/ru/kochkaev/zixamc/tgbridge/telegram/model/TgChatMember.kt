package ru.kochkaev.zixamc.tgbridge.telegram.model

import com.google.gson.annotations.SerializedName

open class TgChatMember(
    val status: TgChatMemberStatuses,
    open val user: TgUser
)
enum class TgChatMemberStatuses {
    @SerializedName("creator")
    CREATOR {
        override val model: Class<out TgChatMember> = TgChatMemberOwner::class.java
    },
    @SerializedName("administrator")
    ADMINISTRATOR {
        override val model: Class<out TgChatMember> = TgChatMemberAdministrator::class.java
    },
    @SerializedName("member")
    MEMBER {
        override val model: Class<out TgChatMember> = TgChatMemberMember::class.java
    },
    @SerializedName("restricted")
    RESTRICTED {
        override val model: Class<out TgChatMember> = TgChatMemberRestricted::class.java
    },
    @SerializedName("left")
    LEFT {
        override val model: Class<out TgChatMember> = TgChatMemberLeft::class.java
    },
    @SerializedName("kicked")
    BANNED {
        override val model: Class<out TgChatMember> = TgChatMemberBanned::class.java
    };

    abstract val model: Class<out TgChatMember>
}

data class TgChatMemberOwner(
    override val user: TgUser,
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean = false,
    @SerializedName("custom_title")
    val customTitle: String? = null
): TgChatMember(TgChatMemberStatuses.CREATOR, user)
data class TgChatMemberAdministrator(
    override val user: TgUser,
    @SerializedName("can_be_edited")
    val canBeEdited: Boolean = false,
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean = false,
    @SerializedName("custom_title")
    val customTitle: String? = null
): TgChatMember(TgChatMemberStatuses.ADMINISTRATOR, user)
data class TgChatMemberMember(
    override val user: TgUser
): TgChatMember(TgChatMemberStatuses.MEMBER, user)
data class TgChatMemberRestricted(
    override val user: TgUser,
    @SerializedName("is_member")
    val isMember: Boolean = true
): TgChatMember(TgChatMemberStatuses.RESTRICTED, user)
data class TgChatMemberLeft(
    override val user: TgUser
): TgChatMember(TgChatMemberStatuses.LEFT, user)
data class TgChatMemberBanned(
    override val user: TgUser,
    @SerializedName("until_date")
    val untilDate: Int
): TgChatMember(TgChatMemberStatuses.BANNED, user)
