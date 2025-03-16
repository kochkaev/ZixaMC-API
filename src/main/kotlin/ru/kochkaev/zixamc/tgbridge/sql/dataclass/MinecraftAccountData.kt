package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.gson.annotations.SerializedName

data class MinecraftAccountData(
    var nickname: String,
    @SerializedName("account_status")
    var accountStatus: MinecraftAccountType,
)
