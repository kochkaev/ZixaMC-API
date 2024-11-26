package ru.kochkaev.zixamc.requests.dataclassSQL

data class RequestData(
    var message_id_in_target_chat: Long,
    var message_id_in_chat_with_user: Long,
    var request_status: String,
    var request_nickname: String,
)
