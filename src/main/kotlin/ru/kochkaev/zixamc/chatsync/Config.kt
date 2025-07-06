package ru.kochkaev.zixamc.chatsync

import net.fabricmc.loader.api.FabricLoader
import ru.kochkaev.zixamc.api.config.ConfigFile
import ru.kochkaev.zixamc.api.config.TextData
import java.io.File

data class Config(
    val defaultGroup: DefaultGroup = DefaultGroup(),
    val messages: ChatSyncMessage = ChatSyncMessage(),
    val events: ChatSyncGameEvents = ChatSyncGameEvents(),
    val lang: ChatSyncLang = ChatSyncLang(),
    val betaMarkdown: Boolean = false,
    val addPrefixToChatMessages: Boolean = true,
    val reply: ChatSyncReply = ChatSyncReply(),
    val featue: ChatSyncFeatureConfig = ChatSyncFeatureConfig()
) {
    data class DefaultGroup(
        val chatId: Long = 0,
        val topicId: Int? = 0,
        val name: String = "zixa",
        val aliases: List<String> = listOf("main"),
        val prefix: TextData = TextData("<color:aqua>Telegram</color:aqua>"),
        val fromMcPrefix: TextData = TextData("<color:dark_green>Minecraft</color:dark_green>"),
    )
    data class ChatSyncLang (
        val telegram: LangTelegram = LangTelegram(),
        val minecraft: LangMinecraft = LangMinecraft()
    ) {
        data class LangAdvancements(
            val regular: String = "<tg-emoji emoji-id=\"5463071033256848094\">⬆️</tg-emoji> <b>{username} получил достижение '{title}'</b>\n\n<i>{description}</i>",
            val goal: String = "<tg-emoji emoji-id=\"5226431245918942763\">🏆</tg-emoji> <b>{username} достиг цели '{title}'</b>\n\n<i>{description}</i>",
            val challenge: String = "<tg-emoji emoji-id=\"5372965329511139384\">😎</tg-emoji> <b>{username} выполнил челлендж '{title}'</b>\n\n<i>{description}</i>",
        )
        data class LangTelegram(
            val serverStarted: String = "<b><tg-emoji emoji-id=\"5463392464314315076\">👉</tg-emoji> Сервер успешно запущен</b>",
            val serverStopped: String = "<b><tg-emoji emoji-id=\"5462956611033117422\">📀</tg-emoji> Сервер остановлен</b>",
            val playerJoined: String = "<b><tg-emoji emoji-id=\"5462910521739063094\">👋</tg-emoji> {username} залетел</b>",
            val playerLeft: String = "<b><tg-emoji emoji-id=\"5375364347918827433\">👋</tg-emoji> {username} ливнул</b>",
            val playerDied: String = "<b><tg-emoji emoji-id=\"5373239082136650704\">🔪</tg-emoji> {deathMessage}</b>",
            val chatMessage: String = "<b>{username}</b> » {text}",
            val sayMessage: String = "<b><tg-emoji emoji-id=\"5454113432284446338\">✉️</tg-emoji> Объявление!</b>\n<b>{username}</b> » {text}",
            val meMessage: String = "<b><tg-emoji emoji-id=\"5226928895189598791\">⭐️</tg-emoji> {username} {text}</b>",
            val advancements: LangAdvancements = LangAdvancements(),
            val playerList: String = "<b><tg-emoji emoji-id=\"5463412289883353404\">🤟</tg-emoji> Онлайн {count} игроков:</b>\n<i>{usernames}</i>",
            val playerListZeroOnline: String = "<b><tg-emoji emoji-id=\"5463137996091962323\">😭</tg-emoji> Никого нет онлайн.</b>",
        )
        data class LangMinecraft(
            val messageTGFormat: TextData = TextData("[<prefix><reset>] <hover:show_text:'Упомянуть его/её (Shift + клик)'><insert:'@{sender}'>{sender}</insert></hover> » <text>"),
            val messageMCFormat: TextData = TextData("[<prefix><reset>] <hover:show_text:'Написать личное сообщение'><click:suggest_command:'/tell {nickname} '>{nickname}</click></hover> » <text>"),
            val prefixAppend: TextData = TextData("<hover:show_text:'Нажмите, что бы ответить'><click:suggest_command:'/r {group} {message_id} '><prefix></click></hover>"),
            val reply: TextData = TextData("<color:gray><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>    ┌──── {sender} » <text></click></hover></color:gray>"),
            val replyToMinecraft: TextData = TextData("<color:gray><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>    ┌──── <text></click></hover></color:gray>"),
            val forward: TextData = TextData("<color:gray><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>{from} »</click></hover></color:gray>"),
            val gif: TextData = TextData("<color:yellow><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>[GIF]</click></hover></color:yellow>"),
            val document: TextData = TextData("<color:yellow><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>[Документ]</click></hover></color:yellow>"),
            val photo: TextData = TextData("<color:yellow><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>[Фото]</click></hover></color:yellow>"),
            val audio: TextData = TextData("<color:yellow><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>[Аудио]</click></hover></color:yellow>"),
            val sticker: TextData = TextData("<color:yellow><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>[Стикер]</click></hover></color:yellow>"),
            val video: TextData = TextData("<color:yellow><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>[Видео]</click></hover></color:yellow>"),
            val videoMessage: TextData = TextData("<color:yellow><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>[Кружок]</click></hover></color:yellow>"),
            val voiceMessage: TextData = TextData("<color:yellow><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>[Голосовуха]</click></hover></color:yellow>"),
            val poll: TextData = TextData("<color:yellow><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>[Опрос: {title}]</click></hover></color:yellow>"),
            val pin: TextData = TextData("<color:gray><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'>закреплено сообщение » <text></click></hover></color:gray>"),
            val link: TextData = TextData("<color:yellow><underlined><hover:show_text:'Открыть в браузере'><click:open_url:'{url}'><title></click></hover></underlined></color:yellow>"),
            val mention: TextData = TextData("<color:yellow><insert:'{mention}'><hover:show_text:'Упомянуть его/её (Shift + клик)'><title></hover></insert></color:yellow>"),
            val hashtag: TextData = TextData("<color:yellow><hover:show_text:'Открыть в Telegram'><click:open_url:'{url}'><title></click></hover></color:yellow>"),
            val code: TextData = TextData("<color:gray><hover:show_text:'Копировать в буфер обмена'><click:copy_to_clipboard:'{text}'><title></click></hover></color:gray>"),
            val spoiler: TextData = TextData("<color:gray><obfuscated><hover:show_text:<text>>{placeholder}</hover></obfuscated></color:gray>"),
            val spoilerReplaceWithChar: String? = "▌",
        )
    }
    data class ChatSyncMessage (
        val bluemapUrl: String? = null,
        val requirePrefixInMinecraft: String? = "",
        val keepPrefix: Boolean = false,
        val mergeWindow: Int? = 0,
        val replyInDifferentLine: Boolean = false,
        val styledTelegramMessagesInMinecraft: Boolean = true,
        val parseMarkdownInMinecraftToTelegramMessages: Boolean = true,
    )
    data class ChatSyncGameEvents (
        val advancementMessages: ChatSyncAdvancements = ChatSyncAdvancements(),
        val enableDeathMessages: Boolean = true,
        val enableJoinMessages: Boolean = true,
        val enableLeaveMessages: Boolean = true,
        val leaveJoinMergeWindow: Int? = 0,
    ) {
        data class ChatSyncAdvancements(
            val enable: Boolean = true,
            val enableTask: Boolean = true,
            val enableGoal: Boolean = true,
            val enableChallenge: Boolean = true,
            val showDescription: Boolean = true,
        )
    }
    data class ChatSyncReply(
        val chatNotFound: TextData = TextData("<color:gray><italic>Чат \"{group}\" не найден!</italic></color:gray>"),
        val errorDueSending: TextData = TextData("<color:gray><italic>Возникла ошибка во время отправки вашего сообщения</italic></color:gray>"),
        val messageIdNotFound: TextData = TextData("<color:gray><italic>Ошибка: сообщение с id {messageId} не найдено в группе {group}</italic></color:gray>"),
    )
    data class ChatSyncFeatureConfig(
        val display: String = "Синхронизация чата 💬",
        val description: String = "<b>Вы можете связать эту группу с чатом Minecraft сервера! 💬</b>\nСообщения из этой группы будут видны только её членам, также как и написать в эту группу из Minecraft смогут только её члены.\n\n<i>Для отправки сообщения из Minecraft, выполните:</i>\n<code>/r {groupName} &lt;Сообщение&gt;</code>",
        val doneTopic: String = "<b>Готово!</b> 🎉\nТеперь этот топик синхронизирован с чатом сервера!\nВы можете изменить параметры функции в настройках » /settings",
        val doneNoTopic: String = "<b>Готово!</b> 🎉\nТеперь эта группа синхронизирована с чатом сервера!\nВы можете изменить параметры функции в настройках » /settings",
        val prefixNeeded: String = "Отправьте в ответ на это сообщение <u>префикс</u>, который будет добавлен к сообщениям из этой группы в Minecraft, в формате MiniMessage. 🎨\n<i><a href=\"https://docs.advntr.dev/minimessage/format.html\">Подробнее о MiniMessage...</a></i>\n\nК примеру:\n<code>&lt;#FFFF55&gt;&lt;bold&gt;{groupName}</code>\n<i>[{groupName}] &lt;Отправитель&gt; » &lt;Сообщение&gt;</i>",
        val wrongPrefix: String = "Ошибка в формате MiniMessage!\n<i>{error}</i>",
        val options: String = "» ID топика -> <code>{topicId}</code>\n» Префикс -> <code>{prefix}</code>\n» Префикс (Minecraft) -> <code>{fromMcPrefix}</code>\n\n<i>Префикс (Minecraft) — префикс, отображаемый в Minecraft для сообщений, отправленных из Minecraft. Если не установлен, то используется обычный префикс.</i>",
        val editPrefix: String = "Изменить префикс 🎨",
        val editPrefixMC: String = "Изменить префикс (Minecraft) ✏️",
    )

    companion object: ConfigFile<Config>(
        file = File(FabricLoader.getInstance().configDir.toFile(), "ZixaMC-ChatSync.json"),
        model = Config::class.java,
        supplier = ::Config
    )
}
