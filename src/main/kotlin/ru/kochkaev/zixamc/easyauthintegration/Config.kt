package ru.kochkaev.zixamc.easyauthintegration

import net.fabricmc.loader.api.FabricLoader
import ru.kochkaev.zixamc.api.config.ConfigFile
import ru.kochkaev.zixamc.api.config.TextData
import java.io.File

data class Config(
    val suppressMessagesWithoutAuth: Boolean = false,
    val langMinecraft: ServerBotEasyAuthLangMinecraft = ServerBotEasyAuthLangMinecraft(),
    val langTelegram: ServerBotEasyAuthLangTelegram = ServerBotEasyAuthLangTelegram(),
) {
    data class ServerBotEasyAuthLangMinecraft (
        val onApprove: TextData = TextData("<color:green>Вы были авторизованы через Telegram, хорошей игры!</color:green>"),
        val onDeny: TextData = TextData("Вход был запрещён в Telegram."),
        val youAreNotPlayer: TextData = TextData("Вы не являетесь игроком сервера!\nЕсли это ошибка, обратитесь за помощю к администратору."),
        val onJoinTip: TextData = TextData("Войдите в 1 клик, используя Telegram!"),
        val noHaveChatWithBot: TextData = TextData("<color:yellow><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>Похоже, у вас нет диалога с Telegram ботом... \nДля быстрой авторизации на сервере, <underlined>нажмите на это сообщение</underlined>, перейдите в чат с ботом и нажмите \"Начать\", после чего, перезайдите на сервер.</click></hover></color:yellow>"),
        val botUsername: String = "@zixamc_beta_bot",
    )
    data class ServerBotEasyAuthLangTelegram (
        val onApprove: String = "Вход {nickname} разрешён! ✅",
        val onDeny: String = "Вход {nickname} запрещён! ❌",
        val onJoinTip: String = "<b>Кто-то пытается войти на сервер</b>, используя ваш аккаунт <i>\"{nickname}\"</i>. 👮‍♂️\nЭто вы?",
        val buttonApprove: String = "Это я ✅",
        val buttonDeny: String = "Это не я ❌",
    )
    companion object: ConfigFile<Config>(
        file = File(FabricLoader.getInstance().configDir.toFile(), "ZixaMC-EasyAuthIntegration.json"),
        model = Config::class.java,
        supplier = ::Config
    )
}
