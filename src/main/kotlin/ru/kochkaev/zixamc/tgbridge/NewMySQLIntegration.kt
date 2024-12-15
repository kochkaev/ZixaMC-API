package ru.kochkaev.zixamc.tgbridge

import ru.kochkaev.zixamc.tgbridge.dataclassSQL.*

object NewMySQLIntegration {

    private val sql = NewMySQL()
    lateinit var linkedEntities: HashMap<Long, NewSQLEntity>

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
        if (!sql.isUserRegistered(userId)) linkedEntities[userId] = NewSQLEntity(sql, userId, accountType)
    }

    fun addRequest(userId: Long, requestData: RequestData) {
        if (!sql.isUserRegistered(userId)) addRequester(userId)
        linkedEntities[userId]!!.addRequest(requestData)
    }
    fun isAgreedWithRules(userId: Long): Boolean =
        linkedEntities[userId]?.data?.agreedWithRules ?: false
    fun setAgreedWithRules(userId: Long, agreed: Boolean) {
        if (!sql.isUserRegistered(userId)) addRequester(userId)
        val entity = linkedEntities[userId]!!
        entity.data = entity.createAndOrGetData().apply { this.agreedWithRules = agreed }
    }

    fun isAdmin(userId: Long): Boolean = linkedEntities[userId]?.accountType == 0

    fun getLinkedEntity(userId: Long): NewSQLEntity? = linkedEntities[userId]
    fun getOrRegisterLinkedEntity(userId: Long): NewSQLEntity {
        if (!sql.isUserRegistered(userId)) addUser(userId)
        return linkedEntities[userId]!!
    }
    fun getLinkedEntityByTempArrayMessagesId(messageId: Long): NewSQLEntity? {
        val userId: Long = sql.getUserIdByUserTempArrayMember(messageId.toString())?:return null
        return linkedEntities[userId]
    }
    fun getLinkedEntityByNickname(nickname: String): NewSQLEntity? {
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

    fun parseNewDataType(json: String?): NewAccountData =
        if (json != null) sql.gson.fromJson(json, NewAccountData::class.java) else NewAccountData()
}