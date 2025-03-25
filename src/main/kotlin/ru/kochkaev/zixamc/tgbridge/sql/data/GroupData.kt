package ru.kochkaev.zixamc.tgbridge.sql.data

import com.google.gson.annotations.SerializedName
import java.util.*

class GroupData (
    var isPrivate: Boolean = true,
    var greetingEnable: Boolean = true,
    protected: EnumMap<AccountType, ArrayList<NewProtectedData>> = EnumMap(AccountType::class.java)
): ChatData(protected)