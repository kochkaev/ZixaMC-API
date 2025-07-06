package ru.kochkaev.zixamc.api.sql.process

import com.google.gson.annotations.JsonAdapter
import ru.kochkaev.zixamc.api.config.serialize.ProcessTypeAdapter
import ru.kochkaev.zixamc.api.sql.SQLProcess
import ru.kochkaev.zixamc.api.telegram.model.TgMessage

@JsonAdapter(ProcessTypeAdapter::class)
open class ProcessType<R: ProcessData>(
    val model: Class<R>,
    val serializedName: String,
    val processorType: ProcessorType = ProcessorType.NONE,
    val processor: (suspend (TgMessage, SQLProcess<R>, R) -> Unit)? = null,
    val cancelOnMenuSend: Boolean = false,
)
