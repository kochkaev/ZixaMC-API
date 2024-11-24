package ru.kochkaev.zixamc.requests.dataclassSQL

data class RequestData(
    val message_id_in_target_chat: Long,
    val message_id_in_chat_with_user: Long,
    val request_status: String,
    val request_nickname: String,
)
