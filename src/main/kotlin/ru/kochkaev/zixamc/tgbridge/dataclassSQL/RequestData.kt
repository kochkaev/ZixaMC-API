package ru.kochkaev.zixamc.tgbridge.dataclassSQL

data class RequestData(
    public var message_id_in_target_chat: Long?,
    public var message_id_in_chat_with_user: Long,
    public var request_status: String,
    public var request_nickname: String?,
)
