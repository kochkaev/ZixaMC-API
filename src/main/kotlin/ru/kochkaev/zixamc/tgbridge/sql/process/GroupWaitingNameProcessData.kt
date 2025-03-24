package ru.kochkaev.zixamc.tgbridge.sql.process

import com.google.gson.annotations.SerializedName

data class GroupWaitingNameProcessData(
    override val messageId: Int,
    val nameType: NameType,
): ProcessData(messageId) {
    enum class NameType {
        @SerializedName("name")
        NAME,
        @SerializedName("alias")
        ALIAS;
    }
}
