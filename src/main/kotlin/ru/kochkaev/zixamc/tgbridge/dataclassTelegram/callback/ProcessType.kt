package ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback

import com.google.gson.annotations.JsonAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.ProcessTypeAdapter

@JsonAdapter(ProcessTypeAdapter::class)
data class ProcessType<R: ProcessData>(
    val model: Class<R>,
    val serializedName: String,
)
