package ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback

import com.google.gson.annotations.SerializedName

data class GroupWaitingNameProcessData(
    val messageId: Int,
    val callbacks: List<Long>,
    val nameType: NameType,
): ProcessData {
    enum class NameType {
        @SerializedName("name")
        NAME,
        @SerializedName("alias")
        ALIAS;
    }
}
