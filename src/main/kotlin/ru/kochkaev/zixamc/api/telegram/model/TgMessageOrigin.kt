package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.serialize.TgMessageOriginAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTypeEnum

/** This object describes the origin of a message. It can be one of, MessageOriginUser, MessageOriginHiddenUser, MessageOriginChat or MessageOriginChannel */
@JsonAdapter(TgMessageOriginAdapter::class)
open class TgMessageOrigin(
    /** Type of the message origin */
    val type: TgMessageOriginType,
    /** Date the message was sent originally in Unix time */
    val date: Int,
)
enum class TgMessageOriginType: TgTypeEnum {
    @SerializedName("user")
    USER {
        override val model = TgMessageOriginUser::class.java
    },
    @SerializedName("hidden_user")
    HIDDEN_USER {
        override val model = TgMessageOriginHiddenUser::class.java
    },
    @SerializedName("chat")
    CHAT {
        override val model = TgMessageOriginChat::class.java
    },
    @SerializedName("channel")
    CHANNEL {
        override val model = TgMessageOriginChannel::class.java
    },
}

/** The message was originally sent by a known user. */
class TgMessageOriginUser(
    date: Int,
    /** User that sent the message originally */
    @SerializedName("sender_user")
    val senderUser: TgUser,
): TgMessageOrigin(TgMessageOriginType.USER, date)
/** The message was originally sent by an unknown user. */
class TgMessageOriginHiddenUser(
    date: Int,
    /** Name of the user that sent the message originally */
    @SerializedName("sender_user_name")
    val senderUserName: String,
): TgMessageOrigin(TgMessageOriginType.HIDDEN_USER, date)
/** The message was originally sent on behalf of a chat to a group chat. */
class TgMessageOriginChat(
    date: Int,
    /** Chat that sent the message originally */
    @SerializedName("sender_chat")
    val senderChat: TgChat,
    /** For messages originally sent by an anonymous chat administrator, original message author signature */
    @SerializedName("author_signature")
    val authorSignature: String?,
): TgMessageOrigin(TgMessageOriginType.CHAT, date)
/** The message was originally sent to a channel chat. */
class TgMessageOriginChannel(
    date: Int,
    /** Channel chat to which the message was originally sent */
    val chat: TgChat,
    /** Unique message identifier inside the chat */
    @SerializedName("message_id")
    val messageId: Int,
    /** Signature of the original post author */
    @SerializedName("author_signature")
    val authorSignature: String?,
): TgMessageOrigin(TgMessageOriginType.CHANNEL, date)