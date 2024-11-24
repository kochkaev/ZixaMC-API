package ru.kochkaev.zixamc.requests.dataclassSQL

data class RequesterData(
    val agreedWithRules: Boolean,
    val requests: List<RequestData>

) : AccountData()
