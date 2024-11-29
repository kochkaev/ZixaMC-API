package ru.kochkaev.zixamc.requests.dataclassSQL

data class AdminData(
    public var permission_level: Int = 0,
    public var player_data: PlayerData? = null,
): AccountData()
