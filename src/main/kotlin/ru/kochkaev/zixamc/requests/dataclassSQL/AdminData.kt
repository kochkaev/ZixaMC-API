package ru.kochkaev.zixamc.requests.dataclassSQL

data class AdminData(
    var permission_level: Int = 0,
    var player_data: PlayerData? = null,
): AccountData()
