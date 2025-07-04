package ru.kochkaev.zixamc.api.sql.data

import com.google.gson.annotations.SerializedName

data class MinecraftAccountData(
    var nickname: String,
    @SerializedName("account_status")
    var accountStatus: MinecraftAccountType,
)
