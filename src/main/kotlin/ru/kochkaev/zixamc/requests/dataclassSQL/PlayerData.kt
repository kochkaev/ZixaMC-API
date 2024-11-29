package ru.kochkaev.zixamc.requests.dataclassSQL

data class PlayerData(
    public var minecraft_accounts: ArrayList<MinecraftAccountData>,
    public var requester_data: RequesterData? = null,
): AccountData()
