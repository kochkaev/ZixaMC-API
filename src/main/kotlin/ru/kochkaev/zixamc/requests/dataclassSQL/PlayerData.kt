package ru.kochkaev.zixamc.requests.dataclassSQL

data class PlayerData(
    var minecraft_accounts: ArrayList<MinecraftAccountData>,
    var requester_data: RequesterData? = null,
): AccountData()
