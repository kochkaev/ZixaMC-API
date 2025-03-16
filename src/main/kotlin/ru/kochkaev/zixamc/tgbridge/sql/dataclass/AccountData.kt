package ru.kochkaev.zixamc.tgbridge.sql.dataclass

import com.google.gson.annotations.SerializedName
import java.util.*

data class AccountData (
    @SerializedName("minecraft_accounts")
    var minecraftAccounts: ArrayList<MinecraftAccountData> = arrayListOf(),
    var requests: ArrayList<RequestData> = arrayListOf(),
//    @SerializedName("agreed_with_rules")
//    var agreedWithRules: Boolean = false,
    @SerializedName("protected_messages")
    var protectedMessages: ArrayList<ProtectedMessageData> = arrayListOf(),
)