package ru.kochkaev.zixamc.api.config.serialize

import ru.kochkaev.zixamc.api.config.TextData
import ru.kochkaev.zixamc.api.sql.SQLCallback
import ru.kochkaev.zixamc.api.sql.SQLGroup
import ru.kochkaev.zixamc.api.sql.SQLUser
import ru.kochkaev.zixamc.api.sql.data.AccountType
import ru.kochkaev.zixamc.api.sql.process.ProcessType
import ru.kochkaev.zixamc.api.sql.process.ProcessTypes
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureType
import ru.kochkaev.zixamc.tgbridge.telegram.feature.FeatureTypes

class SQLUserAdapter: SimpleAdapter<SQLUser>(
    reader = { SQLUser.getWithoutCheck(it.nextString().toLong()) },
    writer = { out, it -> out.value(it.id) }
)
class SQLGroupAdapter: SimpleAdapter<SQLGroup>(
    reader = { SQLGroup.getWithoutCheck(it.nextString().toLong()) },
    writer = { out, it -> out.value(it.id) }
)
class SQLCallbackAdapter: SimpleAdapter<SQLCallback<*>>(
    reader = { SQLCallback.getWithoutCheck(it.nextString().toLong()) },
    writer = { out, it -> out.value(it.callbackId) }
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