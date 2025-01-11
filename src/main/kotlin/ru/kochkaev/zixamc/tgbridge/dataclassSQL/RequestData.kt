package ru.kochkaev.zixamc.tgbridge.dataclassSQL

data class RequestData(
    var user_request_id: Int,
    var message_id_in_target_chat: Long?,
    var message_id_in_chat_with_user: Long,
    var request_status: RequestType,
    var request_nickname: String?,
)
