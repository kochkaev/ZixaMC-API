package ru.kochkaev.zixamc.tgbridge.legecySQL

import ru.kochkaev.zixamc.tgbridge.dataclassSQL.*

object LegacyMySQLIntegration {

    private val sql = LegacyMySQL()
    private lateinit var linkedEntities: HashMap<Long, LegacySQLEntity>

    fun startServer() {
        sql.connect()
        linkedEntities = sql.getAllLinkedEntities
    }
    fun stopServer() {
        sql.close()
        linkedEntities.clear()
    }

    fun addPlayer(user_id: Long) {
        if (!sql.isUserRegistered(user_id)) linkedEntities[user_id] = LegacySQLEntity(sql, user_id, 1)
    }
    fun addRequester(user_id: Long) {
        if (!sql.isUserRegistered(user_id)) linkedEntities[user_id] = LegacySQLEntity(sql, user_id, 2)
    }
    fun addUser(user_id: Long) {
        if (!sql.isUserRegistered(user_id)) linkedEntities[user_id] = LegacySQLEntity(sql, user_id, 3)
    }

    fun addRequest(user_id: Long, requestData: RequestData) {
        if (!sql.isUserRegistered(user_id)) addRequester(user_id)
        linkedEntities[user_id]!!.addRequest(requestData)
    }
    fun isAgreedWithRules(user_id: Long): Boolean =
        linkedEntities[user_id]?.getRequesterData()?.agreed_with_rules ?: false
    fun setAgreedWithRules(user_id: Long, agreed: Boolean) {
        if (!sql.isUserRegistered(user_id)) addRequester(user_id)
        linkedEntities[user_id]!!.data = modifyData(
            data = linkedEntities[user_id]!!.data,
            accountType = linkedEntities[user_id]!!.account_type,
            insertionAccountTypeLevel = 2,
            insertData = agreed,
            insertField = "agreed_with_rules"
        )
    }

    fun isAdmin(user_id: Long): Boolean = linkedEntities[user_id]?.account_type == 0

    fun getLinkedEntity(user_id: Long): LegacySQLEntity? = linkedEntities[user_id]
    fun getLinkedEntityByTempArrayMessagesId(message_id: Long): LegacySQLEntity? {
        val user_id: Long = sql.getUserIdByUserTempArrayMember(message_id.toString())?:return null
        return linkedEntities[user_id]
    }
    fun getLinkedEntityByNickname(nickname: String): LegacySQLEntity? {
        val user_id: Long = sql.getUserIdByNickname(nickname)?:return null
        return linkedEntities[user_id]
    }
    fun getAllRegisteredUserIds(): List<Long> = linkedEntities.keys.toList()

//    fun promoteToPlayer(user_id: Long, minecraftAccount: MinecraftAccountData) {
//        if (sql.getUserAccountType(user_id) >= 2) {
//            sql.updateUserData(
//                user_id, sql.gson.toJson(
//                    PlayerData(
//                        arrayListOf(minecraftAccount),
//                        if (sql.getUserAccountType(user_id) == 2) parseJsonToPOJO(
//                            sql.getUserData(user_id),
//                            2
//                        ) as RequesterData
//                        else RequesterData(true, arrayListOf())
//                    )
//                )
//            )
//            sql.updateUserAccountType(user_id, 1)
//            sql.updateUserNickname(user_id, minecraftAccount.nickname)
//
//        }
//    }
    fun addMinecraftAccountToPlayer(user_id: Long, minecraftAccount: MinecraftAccountData) {
        if (!sql.isUserRegistered(user_id)) addPlayer(user_id)
        linkedEntities[user_id]!!.addMinecraftAccount(minecraftAccount)
//        if (!sql.isUserRegistered(user_id)) sql.registerUser(user_id, minecraftAccount.nickname, emptyArray(), 1, PlayerData(listOf(minecraftAccount), RequesterData(true, emptyList())))
//        else if (sql.getUserAccountType(user_id) == 2) promoteToPlayer(user_id, minecraftAccount)
//        else sql.updateUserData(user_id, sql.gson.toJson(getPlayerData(parseJsonToPOJO(sql.getUserData(user_id), sql.getUserAccountType(user_id))).let { it!!.minecraft_accounts = it.minecraft_accounts.plus(minecraftAccount) }))
    }

    fun isNicknameTaken(nickname: String): Boolean = sql.isNicknameRegistered(nickname)
    fun isNicknameNotAvailableToTake(user_id: Long, nickname: String): Boolean = sql.isNicknameNotAvailableToRegister(user_id, nickname)
    fun setNickname(user_id: Long, nickname: String) {
        linkedEntities[user_id]!!.addNickname(nickname)
    }

    fun parseJsonToPOJO(json: String?, account_type: Int): LegacyAccountData =
        if (json != null) sql.gson.fromJson(json,
            when (account_type) {
                0 -> LegacyAdminData::class.java
                1 -> LegacyPlayerData::class.java
                2 -> LegacyRequesterData::class.java
                else -> LegacyAccountData::class.java
            }
        ) else LegacyAccountData()

    fun parseNewDataType(json: String?): NewAccountData =
        if (json != null) sql.gson.fromJson(json, NewAccountData::class.java) else NewAccountData()

    fun getLowerTypeData(data: LegacyAccountData?): LegacyAccountData? = when (data) {
        is LegacyAdminData -> data.player_data
        is LegacyPlayerData -> data.requester_data
        else -> data
    }
    fun createHigherTypeData(data: LegacyAccountData?): LegacyAccountData? = when (data) {
        is LegacyRequesterData -> LegacyPlayerData(arrayListOf(), data)
        is LegacyPlayerData -> LegacyAdminData(0, data)
        null -> null
        else -> LegacyRequesterData(false, arrayListOf())
    }

    fun modifyData(
        data: LegacyAccountData?,
        accountType: Int,
        insertionAccountTypeLevel: Int,
        insertData: Any,
        insertField: String
    ): LegacyAccountData? {
        if (data == null) return null
        if (accountType == insertionAccountTypeLevel) {
            data::class.java.declaredFields.forEach {
                if (it.name == insertField) {
                    val field = data::class.java.getDeclaredField(insertField)
                    field.isAccessible = true
                    field.set(data, insertData)
                    return data
                }
            }
        }
        else if (accountType<insertionAccountTypeLevel) {
            val modified = modifyData(
                getLowerTypeData(data) ?: return data,
                accountType + 1,
                insertionAccountTypeLevel,
                insertData,
                insertField
            )
            when (data) {
                is LegacyAdminData -> data.player_data = (modified as LegacyPlayerData)
                is LegacyPlayerData -> data.requester_data = (modified as LegacyRequesterData)
            }
            return data
        }
        return data
    }
}