package ru.kochkaev.zixamc.tgbridge.telegram.feature.data

import ru.kochkaev.zixamc.api.sql.SQLGroup

class PlayersGroupFeatureData (
    var autoAccept: Boolean = true,
    var autoRemove: Boolean = false,
    group: SQLGroup? = null,
): FeatureData(group)
