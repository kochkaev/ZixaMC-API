package ru.kochkaev.zixamc.requests

import com.google.gson.reflect.TypeToken
import ru.kochkaev.zixamc.api.sql.chatdata.ChatDataType
import ru.kochkaev.zixamc.api.sql.data.RequestData
import java.util.ArrayList

object RequestsChatDataType: ChatDataType<ArrayList<RequestData>>(
    model = object: TypeToken<ArrayList<RequestData>>(){}.type,
    serializedName = "requests",
)