namespace ru.kochkaev.zixamc.requests.dataclassSQL

data class NewAccountData (
    val minecraft_accounts: List<MinecraftAccountData>
    val requests: List<RequestData>
    val agreed_with_rules: Boolean = false
) : AccountData()