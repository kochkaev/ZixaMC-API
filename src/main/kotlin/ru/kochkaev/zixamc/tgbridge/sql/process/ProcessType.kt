package ru.kochkaev.zixamc.tgbridge.sql.process

import com.google.gson.annotations.JsonAdapter
import ru.kochkaev.zixamc.tgbridge.config.serialize.ProcessTypeAdapter
import ru.kochkaev.zixamc.tgbridge.sql.SQLProcess
import ru.kochkaev.zixamc.tgbridge.telegram.model.TgMessage

@JsonAdapter(ProcessTypeAdapter::class)
data class ProcessType<R: ProcessData>(
    val model: Class<R>,
    val serializedName: String,
    val processorType: ProcessorType = ProcessorType.NONE,
    val processor: (suspend (TgMessage, SQLProcess<*>, ProcessData) -> Unit)? = null,
)
