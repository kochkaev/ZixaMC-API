package ru.kochkaev.zixamc.requests.dataclassSQL

data class PlayerData(
    val minecraft_accounts: List<AccountData>,
    val requester_data: RequesterData? = null,
): AccountData()
