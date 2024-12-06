package ru.kochkaev.zixamc.tgbridge.legecySQL

import ru.kochkaev.zixamc.tgbridge.dataclassSQL.MinecraftAccountData

data class PlayerData(
    public var minecraft_accounts: ArrayList<MinecraftAccountData>,
    public var requester_data: RequesterData? = null,
): AccountData()
