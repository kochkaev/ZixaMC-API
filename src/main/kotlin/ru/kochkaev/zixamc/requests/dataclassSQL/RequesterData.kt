package ru.kochkaev.zixamc.requests.dataclassSQL

data class RequesterData(
    public var agreed_with_rules: Boolean,
    public var requests: ArrayList<RequestData>
) : AccountData()
