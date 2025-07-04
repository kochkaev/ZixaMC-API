package ru.kochkaev.zixamc.api.sql.data

import java.util.*

class GroupData (
    var isPrivate: Boolean = true,
    var greetingEnable: Boolean = true,
    protected: EnumMap<AccountType, ArrayList<NewProtectedData>> = EnumMap(AccountType::class.java)
): ChatData(protected)