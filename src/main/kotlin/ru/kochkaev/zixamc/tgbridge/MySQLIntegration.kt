package ru.kochkaev.zixamc.tgbridge

import ru.kochkaev.zixamc.tgbridge.dataclassSQL.*

object MySQLIntegration {

    private val sql = MySQL()
    lateinit var linkedEntities: HashMap<Long, SQLEntity>

    fun startServer() {
        sql.connect()
        linkedEntities = sql.getAllLinkedEntities
    }
    fun stopServer() {
        sql.close()
        linkedEntities.clear()
    }

    fun addPlayer(userId: Long) = addUser(userId, 1)
    fun addRequester(userId: Long)  = addUser(userId, 2)
    fun addUser(userId: Long) = addUser(userId, 3)
    fun addUser(userId: Long, accountType: Int) {
        if (!sql.isUserRegistered(userId)) linkedEntities[userId] = SQLEntity(sql, userId, accountType)
    }

    fun getOrAddUser(userId: Long) : SQLEntity = getOrAddUser(userId, 3)
    fun getOrAddUser(userId: Long, accountType: Int) : SQLEntity {
        if (!sql.isUserRegistered(userId)) linkedEntities[userId] = SQLEntity(sql, userId, accountType)
        return linkedEntities[userId]!!
    }

    fun addRequest(userId: Long, requestData: RequestData) {
        if (!sql.isUserRegistered(userId)) addRequester(userId)
        linkedEntities[userId]!!.addRequest(requestData)
    }
//    fun isAgreedWithRules(userId: Long): Boolean =
//        linkedEntities[userId]?.data?.agreedWithRules ?: false
//    fun setAgreedWithRules(userId: Long, agreed: Boolean) {
//        if (!sql.isUserRegistered(userId)) addRequester(userId)
//        val entity = linkedEntities[userId]!!
//        entity.data = entity.createAndOrGetData().apply { this.agreedWithRules = agreed }
//    }

    fun isAdmin(userId: Long): Boolean = linkedEntities[userId]?.accountType == 0

    fun getLinkedEntity(userId: Long): SQLEntity? = linkedEntities[userId]
    fun getOrRegisterLinkedEntity(userId: Long): SQLEntity {
        if (!sql.isUserRegistered(userId)) addUser(userId)
        return linkedEntities[userId]!!
    }
    fun getLinkedEntityByTempArrayMessagesId(messageId: Long): SQLEntity? {
        val userId: Long = sql.getUserIdByUserTempArrayMember(messageId.toString())?:return null
        return linkedEntities[userId]
    }
    fun getLinkedEntityByNickname(nickname: String): SQLEntity? {
        val userId: Long = sql.getUserIdByNickname(nickname)?:return null
        return linkedEntities[userId]
    }
    fun getAllRegisteredUserIds(): List<Long> = linkedEntities.keys.toList()
    fun addMinecraftAccountToPlayer(userId: Long, minecraftAccount: MinecraftAccountData) {
        if (!sql.isUserRegistered(userId)) addPlayer(userId)
        linkedEntities[userId]!!.addMinecraftAccount(minecraftAccount)
    }

    fun isNicknameTaken(nickname: String): Boolean = sql.isNicknameRegistered(nickname)
    fun isNicknameNotAvailableToTake(userId: Long, nickname: String): Boolean = sql.isNicknameNotAvailableToRegister(userId, nickname)
    fun setNickname(userId: Long, nickname: String) {
        linkedEntities[userId]!!.addNickname(nickname)
    }
    fun getAllActiveNicknamesOfUser(userId: Long): List<String> =
        linkedEntities[userId]?.data?.minecraftAccounts?.filter { it.accountStatus == "player" || it.accountStatus == "admin" }?.map { it.nickname } ?: listOf()
    fun getAllFrozenNicknamesOfUser(userId: Long): List<String> =
        linkedEntities[userId]?.data?.minecraftAccounts?.filter { it.accountStatus == "frozen" }?.map { it.nickname } ?: listOf()

    fun parseNewDataType(json: String?): AccountData =
        if (json != null) sql.gson.fromJson(json, AccountData::class.java) else AccountData()
}