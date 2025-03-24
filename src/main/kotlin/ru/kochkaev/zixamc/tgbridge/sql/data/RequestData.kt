package ru.kochkaev.zixamc.tgbridge.sql.data

data class RequestData(
    var user_request_id: Int,
    var message_id_in_target_chat: Long?,
    var message_id_in_chat_with_user: Long,
    var request_message_id_in_chat_with_user: Long?,
    var message_id_in_moderators_chat: Long?,
    var poll_message_id: Long?,
    var request_status: RequestType,
    var request_nickname: String?,
)
