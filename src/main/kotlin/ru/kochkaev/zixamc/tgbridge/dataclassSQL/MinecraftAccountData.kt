package ru.kochkaev.zixamc.tgbridge.dataclassSQL

import com.google.gson.annotations.SerializedName

data class MinecraftAccountData(
    var nickname: String,
    @SerializedName("account_status")
    var accountStatus: String,
)
