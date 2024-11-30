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
    var nicknames: Array<String>?
        get() = sql.getUserSecondNicknames(user_id)
        set(nicknames) {
            sql.updateUserSecondNicknames(user_id, nicknames)
        }
    var account_type: Int
        get() = sql.getUserAccountType(user_id)
        set(account_type) {
            sql.updateUserAccountType(user_id, account_type)
        }
    var temp_array: Array<String>?
        get() = sql.getUserTempArray(user_id)
        set(temp_array) {
            sql.updateUserTempArray(user_id, temp_array)
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
                2 -> RequesterData(false, arrayListOf())
                else -> AccountData()
            })
        )
    }

    fun addSecondNickname(nickname: String) : Boolean = sql.addUserSecondNickname(user_id, nickname)
    fun removeSecondNickname(nickname: String) = sql.updateUserSecondNicknames(user_id, nicknames?.filter { it!=nickname }?.toTypedArray())
    fun setPreferNickname(nickname: String) {
        if ((getPlayerData()?.minecraft_accounts?:return).stream().anyMatch{it.nickname == nickname}) {
            addNickname(nickname)
        }
    }
    fun addNickname(nickname: String) {
        if (nicknames?.contains(nickname) == false) addSecondNickname(nickname)
//        val currentNickname = this.nickname
//        if (currentNickname!=null) addSecondNickname(currentNickname)
        this.nickname = nickname
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
    fun getOrCreateAdminData(): AdminData = when (account_type) {
        0 -> data as AdminData
        else -> {
            promote(0)
            data as AdminData
        }
    }
    fun getOrCreatePlayerData(): PlayerData {
        when (account_type) {
            0 -> if (getOrCreateAdminData().player_data == null) {
                data = modifyData(
                    data = data,
                    accountType = account_type,
                    insertionAccountTypeLevel = 0,
                    insertData = PlayerData(arrayListOf()),
                    insertField = "player_data",
                ) ?: data
            }
            2,3 -> promote(1)
        }
        return getPlayerData()!!
    }
    fun getOrCreateRequesterData(): RequesterData {
        when (account_type) {
            0,1 -> if (getOrCreatePlayerData().requester_data == null) {
                data = modifyData(
                    data = data,
                    accountType = account_type,
                    insertionAccountTypeLevel = 1,
                    insertData = RequesterData(true, arrayListOf()),
                    insertField = "requester_data",
                ) ?: data
            }
            3 -> promote(2)
        }
        return getRequesterData()!!
    }

    fun addMinecraftAccount(account: MinecraftAccountData): Boolean {
        val accounts = getOrCreatePlayerData().minecraft_accounts
        if (accounts.stream().anyMatch{it.nickname == account.nickname}) return false
        else accounts.add(account)
        if (nicknames?.contains(account.nickname) == false) addSecondNickname(account.nickname)
        if (nickname == null) nickname = account.nickname
        data = modifyData(
            data = data,
            accountType = account_type,
            insertionAccountTypeLevel = 1,
            insertData = accounts,
            insertField = "minecraft_accounts",
        ) ?: data
        return true
    }
    fun addRequest(request_data: RequestData) {
        val requests = (getOrCreateRequesterData()).requests
        requests.add(request_data)
        data = modifyData(
            data = data,
            accountType = account_type,
            insertionAccountTypeLevel = 2,
            insertData = requests,
            insertField = "requests",
        ) ?: data
//        when (account_type) {
//            3 -> data = RequesterData(true, arrayListOf(request_data))
//            2 -> data = data.apply { (this as RequesterData?)?.requests?.add(request_data) }
//            1 -> data = data.apply { (this as PlayerData?)?.requester_data?.requests?.add(request_data) }
//            0 -> data = data.apply { (this as AdminData?)?.player_data?.requester_data?.requests?.add(request_data) }
//        }
    }
    fun editRequest(request_data: RequestData) {
        val requests = (getRequesterData()?:return).requests
        when (request_data.request_status) {
            "creating" -> {
                requests.removeIf {it.request_status == "creating"}
                requests.add(request_data)
            }
            else -> {
                requests.removeIf {it.message_id_in_chat_with_user == request_data.message_id_in_chat_with_user}
                requests.add(request_data)
            }
        }
        data = modifyData(
            data = data,
            accountType = account_type,
            insertionAccountTypeLevel = 2,
            insertData = requests,
            insertField = "requests",
        ) ?: data
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

    fun addToTempArray(value: String) : Boolean = sql.addToUserTempArray(user_id, value)
}