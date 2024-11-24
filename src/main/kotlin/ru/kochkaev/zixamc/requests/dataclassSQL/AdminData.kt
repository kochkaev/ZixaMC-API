package ru.kochkaev.zixamc.requests.dataclassSQL

data class AdminData(
    val permission_level: Int = 0,
    val player_data: PlayerData? = null,
): AccountData()
