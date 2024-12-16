package ru.kochkaev.zixamc.tgbridge.legecySQL

import ru.kochkaev.zixamc.tgbridge.dataclassSQL.MinecraftAccountData

data class LegacyPlayerData(
    public var minecraft_accounts: ArrayList<MinecraftAccountData>,
    public var requester_data: LegacyRequesterData? = null,
): LegacyAccountData()
