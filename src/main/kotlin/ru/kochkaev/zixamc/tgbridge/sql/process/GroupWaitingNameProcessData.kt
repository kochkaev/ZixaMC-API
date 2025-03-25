package ru.kochkaev.zixamc.tgbridge.sql.process

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
