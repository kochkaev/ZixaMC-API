package ru.kochkaev.zixamc.tgbridge.dataclassSQL

import com.google.gson.annotations.SerializedName

enum class RequestType {
    @SerializedName("creating")
    CREATING {
        override fun getName(): String = "creating"
        override fun shouldBeSingleActive(): Boolean = true
    },
    @SerializedName("moderating")
    MODERATING {
        override fun getName(): String = "moderating"
        override fun shouldBeSingleActive(): Boolean = true
    },
    @SerializedName("pending")
    PENDING {
        override fun getName(): String = "pending"
        override fun shouldBeSingleActive(): Boolean = true
    },
    @SerializedName("accepted")
    ACCEPTED {
        override fun getName(): String = "accepted"
        override fun shouldBeSingleActive(): Boolean = false
    },
    @SerializedName("rejected")
    REJECTED {
        override fun getName(): String = "rejected"
        override fun shouldBeSingleActive(): Boolean = false
    },
    @SerializedName("canceled")
    CANCELED {
        override fun getName(): String = "canceled"
        override fun shouldBeSingleActive(): Boolean = false
    },
    @SerializedName("denied")
    DENIED {
        override fun getName(): String = "denied"
        override fun shouldBeSingleActive(): Boolean = false
    };

    abstract fun getName():String
    abstract fun shouldBeSingleActive():Boolean

    companion object {
        fun parse(name: String): RequestType? = when (name) {
            "creating" -> CREATING
            "moderating" -> MODERATING
            "pending" -> PENDING
            "accepted" -> ACCEPTED
            "rejected" -> REJECTED
            "canceled" -> CANCELED
            else -> null
        }
        fun getAllPending():List<RequestType> = listOf(MODERATING, PENDING)
        fun getAllPendingAndCreating():List<RequestType> = listOf(CREATING, MODERATING, PENDING)
        fun getAllDone():List<RequestType> = listOf(ACCEPTED, REJECTED, CANCELED)
    }
}