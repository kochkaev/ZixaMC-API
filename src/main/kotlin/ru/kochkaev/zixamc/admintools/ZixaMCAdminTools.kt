package ru.kochkaev.zixamc.admintools

import net.fabricmc.api.ModInitializer
import ru.kochkaev.zixamc.api.config.ConfigManager
import ru.kochkaev.zixamc.api.telegram.AdminPanel

class ZixaMCAdminTools: ModInitializer {
    override fun onInitialize() {
        ConfigManager.registerConfig(Config)

        AdminPanel.addIntegration(AdminPanel.Integration.of(
            callbackName = $$"admintools$usersManager",
            display = Config.config.manager.users.panelButton,
            customDataType = AdminManager.UsersManagerCallback::class.java,
            customDataInitial = AdminManager.UsersManagerCallback(),
            processor = AdminManager::onCallbackUsers
        ))
        AdminPanel.addIntegration(AdminPanel.Integration.of(
            callbackName = $$"admintools$groupsManager",
            display = Config.config.manager.groups.panelButton,
            customDataType = AdminManager.GroupsManagerCallback::class.java,
            customDataInitial = AdminManager.GroupsManagerCallback(),
            processor = AdminManager::onCallbackGroups
        ))
        AdminPanel.addIntegration(AdminPanel.Integration.of(
            callbackName = $$"admintools$playersManager",
            display = Config.config.manager.players.panelButton,
            customDataType = AdminManager.PlayersManagerCallback::class.java,
            customDataInitial = AdminManager.PlayersManagerCallback(),
            processor = AdminManager::onCallbackPlayers
        ))
    }
}