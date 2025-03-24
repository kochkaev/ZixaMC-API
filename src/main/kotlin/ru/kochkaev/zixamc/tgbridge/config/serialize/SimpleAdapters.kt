package ru.kochkaev.zixamc.tgbridge.config.serialize

import ru.kochkaev.zixamc.tgbridge.config.TextData
import ru.kochkaev.zixamc.tgbridge.sql.data.AccountType
import ru.kochkaev.zixamc.tgbridge.sql.process.ProcessType
import ru.kochkaev.zixamc.tgbridge.sql.process.ProcessTypes
import ru.kochkaev.zixamc.tgbridge.sql.util.LinkedCallback
import ru.kochkaev.zixamc.tgbridge.sql.util.LinkedGroup
import ru.kochkaev.zixamc.tgbridge.sql.util.LinkedUser
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureType
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureTypes

class LinkedUserAdapter: SimpleAdapter<LinkedUser>(
    reader = { LinkedUser(it.nextString().toLong()) },
    writer = { out, it -> out.value(it.key) }
)
class LinkedGroupAdapter: SimpleAdapter<LinkedGroup>(
    reader = { LinkedGroup(it.nextString().toLong()) },
    writer = { out, it -> out.value(it.key) }
)
class LinkedCallbackAdapter: SimpleAdapter<LinkedCallback>(
    reader = { LinkedCallback(it.nextString().toLong()) },
    writer = { out, it -> out.value(it.key) }
)
class TextDataAdapter: SimpleAdapter<TextData>(
    reader = { TextData(it.nextString()) },
    writer = { out, it -> out.value(it.raw) }
)
class AccountTypeAdapter: SimpleAdapter<AccountType>(
    reader = { AccountType.parse(it.nextInt()) },
    writer = { out, it -> out.value(it.id) }
)
class FeatureTypeAdapter: SimpleAdapter<FeatureType<*>>(
    reader = { FeatureTypes.entries[it.nextString()] },
    writer = { out, it -> out.value(it.serializedName) }
)
class ProcessTypeAdapter: SimpleAdapter<ProcessType<*>>(
    reader = { ProcessTypes.entries[it.nextString()] },
    writer = { out, it -> out.value(it.serializedName) }
)