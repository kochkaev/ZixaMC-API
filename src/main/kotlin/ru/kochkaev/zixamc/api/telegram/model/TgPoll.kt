package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.SerializedName

/** This object contains information about a poll. */
data class TgPoll(
    /** Unique poll identifier */
    val id: String,
    /** Poll question, 1-300 characters */
    val question: String,
    /** Special entities that appear in the question. Currently, only custom emoji entities are allowed in poll questions */
    @SerializedName("question_entities")
    val questionEntities: List<TgEntity>?,
    /** List of poll options */
    val options: List<TgPollOption>? = null,
    /** Total number of users that voted in the poll */
    @SerializedName("total_voter_count")
    val totalVoterCount: Int,
    /** True, if the poll is closed */
    @SerializedName("is_closed")
    val isClosed: Boolean,
    /** True, if the poll is anonymous */
    @SerializedName("is_anonymous")
    val isAnonymous: Boolean,
    /** Poll type, currently can be “regular” or “quiz” */
    val type: TgPollType,
    /** True, if the poll allows multiple answers */
    @SerializedName("allows_multiple_answers")
    val allowsMultipleAnswers: Boolean,
    /** 0-based identifier of the correct answer option. Available only for polls in the quiz mode, which are closed, or was sent (not forwarded) by the bot or to the private chat with the bot. */
    @SerializedName("correct_option_id")
    val correctOptionId: Int?,
    /** Text that is shown when a user chooses an incorrect answer or taps on the lamp icon in a quiz-style poll, 0-200 characters */
    val explanation: String?,
    /** Special entities like usernames, URLs, bot commands, etc. that appear in the explanation */
    @SerializedName("explanation_entities")
    val explanationEntities: String?,
    /** Amount of time in seconds the poll will be active after creation */
    @SerializedName("open_period")
    val openPeriod: Int?,
    /** Point in time (Unix timestamp) when the poll will be automatically closed */
    @SerializedName("close_date")
    val closeDate: Int?,
)
enum class TgPollType {
    @SerializedName("regular")
    REGULAR,
    @SerializedName("quiz")
    QUIZ;
}

/** This object represents an answer of a user in a non-anonymous poll. */
data class TgPollAnswer(
    /** Unique poll identifier */
    @SerializedName("poll_id")
    val pollId: String,
    /** The chat that changed the answer to the poll, if the voter is anonymous */
    @SerializedName("voter_chat")
    val voterChat: TgChat?,
    /** The user that changed the answer to the poll, if the voter isn't anonymous */
    val user: TgUser?,
    /** 0-based identifiers of chosen answer options. May be empty if the vote was retracted. */
    @SerializedName("option_ids")
    val optionIds: List<Int>,
)

/** This object contains information about one answer option in a poll. */
data class TgPollOption (
    /** Option text, 1-100 characters */
    val text: String,
    /** Special entities that appear in the option text. Currently, only custom emoji entities are allowed in poll option texts */
    @SerializedName("text_entities")
    val textEntities: List<TgEntity>?,
    /** Number of users that voted for this option */
    @SerializedName("voter_count")
    val voterCount: Int,
)