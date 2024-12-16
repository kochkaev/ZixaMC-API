package ru.kochkaev.zixamc.tgbridge

import ru.kochkaev.zixamc.tgbridge.dataclassSQL.*
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.TgMessage
import ru.kochkaev.zixamc.tgbridge.legecySQL.*

class NewSQLEntity(val sql: NewMySQL, val userId: Long) {

    var nickname: String?
        get() = sql.getUserNickname(userId)
        set(nickname) {
//            if (nickname != null) sql.addUserSecondNickname(userId, nickname)
            sql.updateUserNickname(userId, nickname)
        }
    var nicknames: Array<String>?
        get() = sql.getUserSecondNicknames(userId)
        set(nicknames) {
            sql.updateUserSecondNicknames(userId, nicknames)
        }
    var accountType: Int
        get() = sql.getUserAccountType(userId)
        set(accountType) {
            sql.updateUserAccountType(userId, accountType)
        }
    var tempArray: Array<String>?
        get() = sql.getUserTempArray(userId)
        set(tempArray) {
            sql.updateUserTempArray(userId, tempArray)
        }
    var agreedWithRules: Boolean
        get() = sql.isUserAgreedWithRules(userId)
        set(agreedWithRules) {
            sql.updateUserAgreedWithRules(userId, agreedWithRules)
        }
    var isRestricted: Boolean
        get() = sql.isUserRestricted(userId)
        set(isRestricted) {
            sql.updateUserRestricted(userId, isRestricted)
        }
    var rawData: String?
        get() = sql.getUserData(userId)
        set(rawData) {
            sql.updateUserData(userId, rawData)
        }
    var data: NewAccountData?
        get() = LegacyMySQLIntegration.parseNewDataType(rawData)
        set(data) {
            rawData = sql.gson.toJson(data)
        }

    // Add user to table
    constructor(sql: NewMySQL, userId: Long, data: LegacyAccountData) : this(sql, userId, null, data)
    constructor(sql: NewMySQL, userId: Long, nickname: String?, data: LegacyAccountData) : this(sql, userId, nickname, emptyArray(), data)
    constructor(sql: NewMySQL, userId: Long, nickname: String?, nicknames: Array<String>, data: LegacyAccountData) : this(sql, userId) {
        sql.registerUser(
            userId,
            nickname,
            nicknames,
            when (data) {
                is LegacyRequesterData -> 2
                is LegacyPlayerData -> 1
                is LegacyAdminData -> 0
                else -> 3
            },
            sql.gson.toJson(data)
        )
    }
    constructor(sql: NewMySQL, userId: Long, accountType: Int) : this(sql, userId, null, accountType)
    constructor(sql: NewMySQL, userId: Long, nickname: String?, accountType: Int) : this(sql, userId, nickname, emptyArray(), accountType)
    constructor(sql: NewMySQL, userId: Long, nickname: String?, nicknames: Array<String>, accountType: Int) : this(sql, userId) {
        sql.registerUser(
            userId,
            nickname,
            nicknames,
            accountType,
            sql.gson.toJson(when (accountType) {
                0 -> LegacyAdminData(0, null)
                1 -> LegacyPlayerData(arrayListOf(), null)
                2 -> LegacyRequesterData(false, arrayListOf())
                else -> LegacyAccountData()
            })
        )
    }

    fun addToNicknames(nickname: String) : Boolean = sql.addUserSecondNickname(userId, nickname)
    fun removeFromNicknames(nickname: String) = sql.updateUserSecondNicknames(userId, nicknames?.filter { it!=nickname }?.toTypedArray())
    fun setPreferNickname(nickname: String) {
        if ((data?.minecraftAccounts?:return).stream().anyMatch{it.nickname == nickname}) {
            addNickname(nickname)
        }
    }
    fun addNickname(nickname: String) {
        if (nicknames?.contains(nickname) == false) addToNicknames(nickname)
        this.nickname = nickname
    }

    fun createAndOrGetData(): NewAccountData {
        if (data == null) data = NewAccountData()
        return data!!
    }

    fun addMinecraftAccount(account: MinecraftAccountData): Boolean {
        val accounts = createAndOrGetData().minecraftAccounts
        if (accounts.stream().anyMatch{it.nickname == account.nickname}) return false
        else accounts.add(account)
        if (nicknames?.contains(account.nickname) == false) addToNicknames(account.nickname)
        if (nickname == null) nickname = account.nickname
        data = data!!.apply { this.minecraftAccounts = accounts }
        return true
    }
    fun editMinecraftAccount(nickname: String, newStatus: String) {
        val accounts = (data?:return).minecraftAccounts
        val matched = accounts.first { it.nickname == nickname }
        matched.accountStatus = newStatus
        accounts.removeIf { it.nickname == nickname }
        accounts.add(matched)
        data = data!!.apply { this.minecraftAccounts = accounts }
    }
    fun addRequest(requestData: RequestData) {
        val requests = createAndOrGetData().requests
        requests.add(requestData)
        data = data!!.apply { this.requests = requests }
    }
    fun editRequest(requestData: RequestData) {
        val requests = (data?:return).requests
        requests.removeIf {it.user_request_id == requestData.user_request_id}
        requests.add(requestData)
//        when (requestData.request_status) {
//            "creating" -> {
//                requests.removeIf {it.request_status == "creating"}
//                requests.add(requestData)
//            }
//            else -> {
//                requests.removeIf {it.message_id_in_chat_with_user == requestData.message_id_in_chat_with_user}
//                requests.add(requestData)
//            }
//        }
        data = data!!.apply { this.requests = requests }
    }

    fun addToTempArray(value: String) : Boolean = sql.addToUserTempArray(userId, value)

    fun setProtectedInfoMessage(
        message: TgMessage,
        protectLevel: Int,
        protectedType: String,
        senderBotId: Long,
    ) {
        data = data?.apply { this.protectedMessages.add(ProtectedMessageData(
            message_id = message.messageId.toLong(),
            chat_id = message.chat.id,
            protect_level = protectLevel,
            protected_type = protectedType,
            sender_bot_id = senderBotId,
        )) }
    }
}