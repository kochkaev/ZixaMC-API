package ru.kochkaev.zixamc.tgbridge.legecySQL

data class LegacyAdminData(
    public var permission_level: Int = 0,
    public var player_data: LegacyPlayerData? = null,
): LegacyAccountData()
