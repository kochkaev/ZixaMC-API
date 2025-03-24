package ru.kochkaev.zixamc.tgbridge.sql.data

import java.util.*

open class ChatData(
    open val protected: EnumMap<AccountType, ArrayList<NewProtectedData>> = EnumMap(AccountType::class.java)
)
