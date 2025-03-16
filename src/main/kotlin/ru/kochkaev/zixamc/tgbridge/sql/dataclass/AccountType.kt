package ru.kochkaev.zixamc.tgbridge.sql.dataclass

enum class AccountType {
    ADMIN {
        override fun levelHigh(): AccountType? = null
        override fun levelBellow(): AccountType = PLAYER
        override fun getId(): Int = 0
        override fun isPlayer(): Boolean = true
    },
    PLAYER {
        override fun levelHigh(): AccountType = ADMIN
        override fun levelBellow(): AccountType = REQUESTER
        override fun getId(): Int = 1
        override fun isPlayer(): Boolean = true
    },
    REQUESTER {
        override fun levelHigh(): AccountType = PLAYER
        override fun levelBellow(): AccountType = UNKNOWN
        override fun getId(): Int = 2
        override fun isPlayer(): Boolean = false
    },
    UNKNOWN {
        override fun levelHigh(): AccountType = REQUESTER
        override fun levelBellow(): AccountType? = null
        override fun getId(): Int = 3
        override fun isPlayer(): Boolean = false
    };

    abstract fun levelHigh(): AccountType?
    abstract fun levelBellow(): AccountType?
    abstract fun getId():Int
    abstract fun isPlayer():Boolean

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