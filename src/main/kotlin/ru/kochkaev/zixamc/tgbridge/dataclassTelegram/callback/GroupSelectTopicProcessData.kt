package ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback

data class GroupSelectTopicProcessData(
    val messageId: Int,
    val feature: String,
): ProcessData
