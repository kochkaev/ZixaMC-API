package ru.kochkaev.zixamc.tgbridge.legecySQL

import ru.kochkaev.zixamc.tgbridge.dataclassSQL.RequestData

data class RequesterData(
    public var agreed_with_rules: Boolean,
    public var requests: ArrayList<RequestData>
) : AccountData()
