package ru.kochkaev.zixamc.requests

import ru.kochkaev.zixamc.requests.MySQLIntegration.modifyData
import ru.kochkaev.zixamc.requests.dataclassSQL.*

class SQLEntity(val sql: MySQL, val user_id: Long) {

    var nickname: String?
        get() = sql.getUserNickname(user_id)
        set(new_nickname) {
            if (nickname != null) sql.addUserSecondNickname(user_id, nickname)
            sql.updateUserNickname(user_id, new_nickname)
        }
    var second_nicknames: Array<String>?
        get() = sql.getUserSecondNicknames(user_id)
        set(second_nicknames) {
            sql.updateUserSecondNicknames(user_id, second_nicknames)
        }
    var account_type: Int
        get() = sql.getUserAccountType(user_id)
        set(account_type) {
            sql.updateUserAccountType(user_id, account_type)
        }
    var rawData: String?
        get() = sql.getUserData(user_id)
        set(rawData) {
            sql.updateUserData(user_id, rawData)
        }
    var data: AccountData?
        get() = MySQLIntegration.parseJsonToPOJO(rawData, account_type)
        set(data) {
            rawData = sql.gson.toJson(data)
        }

    // Add user to table
    constructor(sql: MySQL, user_id: Long, data: AccountData) : this(sql, user_id, null, data)
    constructor(sql: MySQL, user_id: Long, nickname: String?, data: AccountData) : this(sql, user_id, nickname, emptyArray(), data)
    constructor(sql: MySQL, user_id: Long, nickname: String?, second_nicknames: Array<String>, data: AccountData) : this(sql, user_id) {
        sql.registerUser(
            user_id,
            nickname,
            second_nicknames,
            when (data) {
                is RequesterData -> 2
                is PlayerData -> 1
                is AdminData -> 0
                else -> 3
            },
            sql.gson.toJson(data)
        )
    }
    constructor(sql: MySQL, user_id: Long, account_type: Int) : this(sql, user_id, null, account_type)
    constructor(sql: MySQL, user_id: Long, nickname: String?, account_type: Int) : this(sql, user_id, nickname, emptyArray(), account_type)
    constructor(sql: MySQL, user_id: Long, nickname: String?, second_nicknames: Array<String>, account_type: Int) : this(sql, user_id) {
        sql.registerUser(
            user_id,
            nickname,
            second_nicknames,
            account_type,
            sql.gson.toJson(when (account_type) {
                0 -> AdminData(0, null)
                1 -> PlayerData(arrayListOf(), null)
                2 -> RequesterData(true, arrayListOf())
                else -> AccountData()
            })
        )
    }

    fun addSecondNickname(nickname: String) : Boolean = sql.addUserSecondNickname(user_id, nickname)
    fun removeSecondNickname(nickname: String) = sql.updateUserSecondNicknames(user_id, second_nicknames?.filter { it!=nickname }?.toTypedArray())
    fun addMinecraftAccount(account: MinecraftAccountData): Boolean {
        val accounts = (getPlayerData()?.minecraft_accounts ?: arrayListOf<MinecraftAccountData>().apply {promote(2)})
        if (accounts.stream().anyMatch{it.nickname == account.nickname}) return false
        else accounts.add(account)
        if (second_nicknames?.contains(account.nickname) == false && nickname != account.nickname) addSecondNickname(account.nickname)
        data = modifyData(
            data = data,
            accountType = account_type,
            insertionAccountTypeLevel = 2,
            insertData = accounts,
            insertField = "minecraft_accounts"
        )
        return true
    }
    fun setPreferNickname(nickname: String) {
        if ((getPlayerData()?.minecraft_accounts?:return).stream().anyMatch{it.nickname == nickname}) {
            if (second_nicknames?.contains(nickname) == true) removeSecondNickname(nickname)
            val currentNickname = this.nickname
            if (currentNickname!=null) addSecondNickname(currentNickname)
            this.nickname = nickname
        }
    }
    fun getRequesterData(): RequesterData? = when (account_type) {
        0,1 -> getPlayerData()?.requester_data
        2 -> data as RequesterData?
        else -> null
    }
    fun getPlayerData(): PlayerData? = when (account_type) {
        0 -> getAdminData()?.player_data
        1 -> data as PlayerData?
        else -> null
    }
    fun getAdminData(): AdminData? = if (account_type == 0) data as AdminData? else null

    fun addRequest(request_data: RequestData) {
        val requests = (getRequesterData()?:return).requests
        requests.add(request_data)
        data = MySQLIntegration.modifyData(
            data,
            account_type,
            2,
            requests,
            "requests"
        ) ?: data
//        when (account_type) {
//            3 -> data = RequesterData(true, arrayListOf(request_data))
//            2 -> data = data.apply { (this as RequesterData?)?.requests?.add(request_data) }
//            1 -> data = data.apply { (this as PlayerData?)?.requester_data?.requests?.add(request_data) }
//            0 -> data = data.apply { (this as AdminData?)?.player_data?.requester_data?.requests?.add(request_data) }
//        }
    }

    fun promote(targetAccountType: Int) {
        when (account_type) {
            targetAccountType -> return
            in 0..targetAccountType -> {
                data = MySQLIntegration.getLowerTypeData(data)
                account_type = account_type+1
                promote(targetAccountType)
            }
            else -> {
                data = MySQLIntegration.createHigherTypeData(data)
                account_type = account_type-1
                promote(targetAccountType)
            }
        }
    }
}