package ru.kochkaev.zixamc.api.sql.chatdata

import com.google.gson.reflect.TypeToken
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.data.MinecraftAccountData
import ru.kochkaev.zixamc.api.sql.data.NewProtectedData
import java.util.ArrayList
import java.util.EnumMap

object ChatDataTypes {

    // Chat
    val PROTECTED = ChatDataType<EnumMap<AccountType, ArrayList<NewProtectedData>>>(object: TypeToken<EnumMap<AccountType, ArrayList<NewProtectedData>>>(){}.type, "protected")

    // User
    val MINECRAFT_ACCOUNTS = ChatDataType<ArrayList<MinecraftAccountData>>(object: TypeToken<ArrayList<MinecraftAccountData>>(){}.type, "minecraft_accounts")

    // Group
    val IS_PRIVATE = ChatDataType<Boolean>(Boolean::class.java, "isPrivate")
    val GREETING_ENABLE = ChatDataType<Boolean>(Boolean::class.java, "greetingEnable")

    val entries = hashMapOf<String, ChatDataType<*>>(
        PROTECTED.serializedName to PROTECTED,
        MINECRAFT_ACCOUNTS.serializedName to MINECRAFT_ACCOUNTS,
        IS_PRIVATE.serializedName to IS_PRIVATE,
        GREETING_ENABLE.serializedName to GREETING_ENABLE,
    )
    fun registerType(type: ChatDataType<*>) {
        entries[type.serializedName] = type
    }
}
