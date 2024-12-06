package ru.kochkaev.zixamc.tgbridge.legecySQL

data class AdminData(
    public var permission_level: Int = 0,
    public var player_data: PlayerData? = null,
): AccountData()
