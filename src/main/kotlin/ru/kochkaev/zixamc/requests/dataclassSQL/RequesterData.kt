package ru.kochkaev.zixamc.requests.dataclassSQL

data class RequesterData(
    var agreed_with_rules: Boolean,
    var requests: ArrayList<RequestData>

) : AccountData()
