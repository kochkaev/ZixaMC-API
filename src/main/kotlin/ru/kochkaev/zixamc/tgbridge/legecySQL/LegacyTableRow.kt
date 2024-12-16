package ru.kochkaev.zixamc.tgbridge.legecySQL

data class LegacyTableRow(
    var nickname: String,
    var second_nicknames: Array<String>? = null,
    var account_type: Int = 0,
    var data: LegacyAccountData? = null,
)
