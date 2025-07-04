package ru.kochkaev.zixamc.api.config

data class ConfigSQL(
    val host: String = "",
    val database: String = "",
    val user: String = "",
    val password: String = "",
    val usersTable: String = "users",
    val groupsTable: String = "groups",
    val callbacksTable: String = "callbacks",
    val processesTable: String = "processes",
)