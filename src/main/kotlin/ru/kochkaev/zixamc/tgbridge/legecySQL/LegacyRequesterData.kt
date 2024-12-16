package ru.kochkaev.zixamc.tgbridge.legecySQL

import ru.kochkaev.zixamc.tgbridge.dataclassSQL.RequestData

data class LegacyRequesterData(
    public var agreed_with_rules: Boolean,
    public var requests: ArrayList<RequestData>
) : LegacyAccountData()
