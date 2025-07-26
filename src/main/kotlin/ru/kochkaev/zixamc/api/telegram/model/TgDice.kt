package ru.kochkaev.zixamc.api.telegram.model

/** This object represents an animated emoji that displays a random value. */
data class TgDice(
    /** Emoji on which the dice throw animation is based */
    val emoji: String,
    /** Value of the dice, 1-6 for “🎲”, “🎯” and “🎳” base emoji, 1-5 for “🏀” and “⚽” base emoji, 1-64 for “🎰” base emoji */
    val value: Int,
)
