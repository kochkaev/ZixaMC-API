package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.serialize.TgReactionTypeAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTypeEnum

/** This object describes the type of a reaction. Currently, it can be one of ReactionTypeEmoji, ReactionTypeCustomEmoji or ReactionTypePaid */
@JsonAdapter(TgReactionTypeAdapter::class)
open class TgReactionType(
    /** Type of the reaction */
    val type: TgReactionTypes,
)
enum class TgReactionTypes: TgTypeEnum {
    @SerializedName("emoji")
    EMOJI {
        override val model = TgReactionTypeEmoji::class.java
    },
    @SerializedName("custom_emoji")
    CUSTOM_EMOJI {
        override val model = TgReactionTypeCustomEmoji::class.java
    },
    @SerializedName("paid")
    PAID {
        override val model = TgReactionTypePaid::class.java
    },
}

/** The reaction is based on an emoji. */
class TgReactionTypeEmoji(
    /** Reaction emoji. Currently, it can be one of "❤", "👍", "👎", "🔥", "🥰", "👏", "😁", "🤔", "🤯", "😱", "🤬", "😢", "🎉", "🤩", "🤮", "💩", "🙏", "👌", "🕊", "🤡", "🥱", "🥴", "😍", "🐳", "❤‍🔥", "🌚", "🌭", "💯", "🤣", "⚡", "🍌", "🏆", "💔", "🤨", "😐", "🍓", "🍾", "💋", "🖕", "😈", "😴", "😭", "🤓", "👻", "👨‍💻", "👀", "🎃", "🙈", "😇", "😨", "🤝", "✍", "🤗", "🫡", "🎅", "🎄", "☃", "💅", "🤪", "🗿", "🆒", "💘", "🙉", "🦄", "😘", "💊", "🙊", "😎", "👾", "🤷‍♂", "🤷", "🤷‍♀", "😡" */
    val emoji: String,
): TgReactionType(TgReactionTypes.EMOJI)
/** The reaction is based on a custom emoji. */
class TgReactionTypeCustomEmoji(
    /** Custom emoji identifier */
    @SerializedName("custom_emoji_id")
    val customEmojiId: String,
): TgReactionType(TgReactionTypes.CUSTOM_EMOJI)
/** The reaction is paid. */
class TgReactionTypePaid(): TgReactionType(TgReactionTypes.PAID)
