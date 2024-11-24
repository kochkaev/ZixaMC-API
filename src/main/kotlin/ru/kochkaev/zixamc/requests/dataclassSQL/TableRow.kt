package ru.kochkaev.zixamc.requests.dataclassSQL

data class TableRow(
    val nickname: String,
    val second_nicknames: Array<String>? = null,
    val account_type: Int = 0,
    val data: AccountData? = null,
)
