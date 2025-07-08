package ru.kochkaev.zixamc.admintools

import net.fabricmc.loader.api.FabricLoader
import ru.kochkaev.zixamc.api.config.ConfigFile
import ru.kochkaev.zixamc.api.config.ConfigSQL
import java.io.File

/**
 * @author kochkaev
 */
data class Config (
    val manager: AdminManagerConfig = AdminManagerConfig()
) {
    data class AdminManagerConfig(
        val users: UsersManagerConfig = UsersManagerConfig(),
        val groups: GroupsManagerConfig = GroupsManagerConfig(),
        val players: PlayersManagerConfig = PlayersManagerConfig()
    ) {
        data class UsersManagerConfig(
            val panelButton: String = "Менеджер пользователей 🧔",
        )
        data class GroupsManagerConfig(
            val panelButton: String = "Менеджер групп 👥",
        )
        data class PlayersManagerConfig(
            val panelButton: String = "Менеджер игроков 🎮",
        )
    }

    companion object: ConfigFile<Config>(
        file = File(FabricLoader.getInstance().configDir.toFile(), "ZixaMC-AdminTools.json"),
        model = Config::class.java,
        supplier = ::Config
    )
}