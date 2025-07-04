package ru.kochkaev.zixamc.api.sql.process

import com.google.gson.annotations.SerializedName

class GroupWaitingNameProcessData(
    messageId: Int,
    val nameType: NameType,
): ProcessData(messageId) {
    enum class NameType {
        @SerializedName("name")
        NAME,
        @SerializedName("alias")
        ALIAS;
    }
}
