package ru.kochkaev.zixamc.api.sql.data

import com.google.gson.annotations.SerializedName

enum class MinecraftAccountType {
    @SerializedName("admin")
    ADMIN {
        override fun getName(): String = "admin"
        override fun toAccountType(): AccountType = AccountType.ADMIN
    },
    @SerializedName("player")
    PLAYER {
        override fun getName(): String = "player"
        override fun toAccountType(): AccountType = AccountType.PLAYER
    },
    @SerializedName("old")
    OLD {
        override fun getName(): String = "old"
        override fun toAccountType(): AccountType = AccountType.PLAYER
    },
    @SerializedName("frozen")
    FROZEN {
        override fun getName(): String = "frozen"
        override fun toAccountType(): AccountType = AccountType.REQUESTER
    },
    @SerializedName("banned")
    BANNED {
        override fun getName(): String = "banned"
        override fun toAccountType(): AccountType = AccountType.REQUESTER
    };

    abstract fun getName():String
    abstract fun toAccountType(): AccountType

    companion object {
        fun parse(name: String): MinecraftAccountType? = when (name) {
            "admin" -> ADMIN
            "player" -> PLAYER
            "old" -> OLD
            "frozen" -> FROZEN
            "banned" -> BANNED
            else -> null
        }

        fun getAll(): List<MinecraftAccountType> = listOf(ADMIN, PLAYER, OLD, FROZEN, BANNED)
        fun getAllMaybeActive(): List<MinecraftAccountType> = listOf(ADMIN, PLAYER, FROZEN)
        fun getAllActiveNow(): List<MinecraftAccountType> = listOf(ADMIN, PLAYER)
    }
}