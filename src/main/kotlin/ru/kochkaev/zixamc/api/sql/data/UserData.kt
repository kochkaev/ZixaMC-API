package ru.kochkaev.zixamc.api.sql.data

import com.google.gson.annotations.SerializedName
import java.util.*

class UserData (
    @SerializedName("minecraft_accounts")
    var minecraftAccounts: ArrayList<MinecraftAccountData> = arrayListOf(),
    var requests: ArrayList<RequestData> = arrayListOf(),
    protected: EnumMap<AccountType, ArrayList<NewProtectedData>> = EnumMap(AccountType::class.java),

    // Legacy
    @SerializedName("protected_messages")
    var protectedMessages: ArrayList<ProtectedMessageData>? = null
): ChatData(protected) {
    init {
        if (protectedMessages!=null) {
            protectedMessages!!.forEach { old ->
                val new = NewProtectedData(
                    messageId = old.message_id.toInt(),
                    protectedType = NewProtectedData.ProtectedType.valueOf(old.protected_type.uppercase()),
                    senderBotId = old.sender_bot_id,
                )
                val level = AccountType.parse(old.protect_level)
                protected[level]?.add(new) ?: also { protected[level] = arrayListOf(new) }
            }
            protectedMessages = null
        }
    }
}