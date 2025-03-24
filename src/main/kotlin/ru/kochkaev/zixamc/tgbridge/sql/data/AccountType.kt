package ru.kochkaev.zixamc.tgbridge.sql.data

import com.google.gson.annotations.JsonAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.AccountTypeAdapter

//@JsonAdapter(AccountTypeAdapter::class)
enum class AccountType {
    ADMIN {
        override val levelHigh: AccountType? = null
        override val levelBellow: AccountType get() = PLAYER
        override val id: Int = 0
        override val isPlayer: Boolean = true
        override val requireGroupPrivate: Boolean = true
    },
    PLAYER {
        override val levelHigh: AccountType = ADMIN
        override val levelBellow: AccountType get() = REQUESTER
        override val id: Int = 1
        override val isPlayer: Boolean = true
        override val requireGroupPrivate: Boolean = true
    },
    REQUESTER {
        override val levelHigh: AccountType = PLAYER
        override val levelBellow: AccountType get() = UNKNOWN
        override val id: Int = 2
        override val isPlayer: Boolean = false
        override val requireGroupPrivate: Boolean = false
    },
    UNKNOWN {
        override val levelHigh: AccountType = REQUESTER
        override val levelBellow: AccountType? = null
        override val id: Int = 3
        override val isPlayer: Boolean = false
        override val requireGroupPrivate: Boolean = false
    };

    abstract val levelHigh: AccountType?
    abstract val levelBellow: AccountType?
    abstract val id: Int
    abstract val isPlayer: Boolean
    abstract val requireGroupPrivate: Boolean
    fun isHigherThanOrEqual(than: AccountType): Boolean =
        if (than == this) true
        else this.levelBellow?.isHigherThanOrEqual(than) ?: false

    companion object {
        fun parse(id: Int): AccountType = when (id) {
            0 -> ADMIN
            1 -> PLAYER
            2 -> REQUESTER
            else -> UNKNOWN
        }
        fun parse(name: String): AccountType = when (name) {
            "admin" -> ADMIN
            "player" -> PLAYER
            "requester" -> REQUESTER
            else -> UNKNOWN
        }
    }
}