package ru.kochkaev.zixamc.tgbridge.config

/**
 * @author kochkaev
 */
data class Config (
    val general: GeneralConfig = GeneralConfig(),
    val mySQL: ConfigSQL = ConfigSQL(),
    val requestsBot: RequestsBotDataClass = RequestsBotDataClass(),
    val serverBot: ServerBotDataClass = ServerBotDataClass(),
    val tmp: TempConfig = TempConfig(),
) {
    data class GeneralConfig (
        val serverIP: String = "",
        val lang: GeneralConfigLang = GeneralConfigLang()
    ) {
        data class GeneralConfigLang (
            val infoMessage: String = "<b>Zixa City - Информация о сервере ✨</b>\n\n<i><tg-emoji emoji-id=\"5447410659077661506\">🌐</tg-emoji> IP сервера</i> -> <tg-spoiler>{serverIP}</tg-spoiler>\n<i><tg-emoji emoji-id=\"5415803062738504079\">🗺</tg-emoji> WEB карта</i> -> <a href=\"https://zixamc.dynmap.xyz/\">перейти</a>\n<i><tg-emoji emoji-id=\"5258046117932711905\">📕</tg-emoji> Правила сервера</i> -> <a href=\"https://teletype.in/@zixamc/rules-gaming\">перейти</a>\n<i><tg-emoji emoji-id=\"5284997273938305348\">💎</tg-emoji>Версия Minecraft</i> -> Java Edition 1.21 (Fabric)\n\nРазглашать кому-либо IP сервера или адресс WEB карты категорически <b>Запрещенно!</b>\nЕсли, вдруг, вы случайно где-то спалили это информацию, необходимо сообщить об этом администраторам.\n\n<b>Приятной игры! <tg-emoji emoji-id=\"5465465194056525619\">👍</tg-emoji></b>",
            val buttonCopyServerIP: String = "Копировать IP сервера 📋",
        )
    }
    data class RequestsBotDataClass (
        val isEnabled: Boolean = true,
        val botToken: String = "",
        val botAPIURL: String = "https://api.telegram.org",
        val pollTimeout: Int = 60,
        val playersGroupInviteLink: String = "https://t.me/",
        val addWhitelistCommand: String = "easywhitelist add {nickname}",
        val removeWhitelistCommand: String = "easywhitelist add {nickname}",
        val user: RequestsBotForUser = RequestsBotForUser(),
        val target: RequestsBotForTarget = RequestsBotForTarget(),
        val forModerator: RequestsBotForModerator = RequestsBotForModerator(),
        val commonLang: RequestsBotCommonLang = RequestsBotCommonLang(),
    ) {
        data class RequestsBotForUser (
            val lang: RequestsBotForUserLang = RequestsBotForUserLang(),
        ) {
            data class RequestsBotForUserLang (
                val button: RequestsBotForUserLangButtons = RequestsBotForUserLangButtons(),
                val inputField: RequestsBotForUserLangInputFields = RequestsBotForUserLangInputFields(),
                val event: RequestsBotForUserLangEvents = RequestsBotForUserLangEvents(),
                val creating: RequestsBotForUserLangCreating = RequestsBotForUserLangCreating(),
            ) {
                data class RequestsBotForUserLangButtons (
                    val createRequest: String = "Создать заявку ⚡",
                    val confirmSending: String = "Отправить заявку 🚀",
                    val agreeWithRules: String = "С правилами ознакомлен и согласен ✅",
                    val revokeAgreeWithRules: String = "Отозвать согласие с правилами ⛔",
                    val redrawRequest: String = "Начать заново 📝",
                    val cancelRequest: String = "Отменить заявку ❌",
                    val joinToPlayersGroup: String = "Присоедениться к группе игроков ✈️",
                )
                data class RequestsBotForUserLangInputFields (
                    val enterNickname: String = "Введите никнейм...",
                    val enterRequestText: String = "Напишите заявку...",
                )
                data class RequestsBotForUserLangEvents (
                    val onStart: String = "<b>Приветствую! <tg-emoji emoji-id=\"5462910521739063094\">👋</tg-emoji></b>\n\nХотели стать игроком приватного Minecraft сервера Zixa City? Вы пришли по адресу! <tg-emoji emoji-id=\"5285291543622601498\">👍</tg-emoji>\n\nДля того, что бы отправить свою заявку, нажмите на кнопку <tg-emoji emoji-id=\"5197474438970363734\">⤵️</tg-emoji>",
                    val onSend: String = "<b>Заявка успешно отправлена на модерацию администрации! <tg-emoji emoji-id=\"5258203794772085854\">⚡️</tg-emoji></b>",
                    val onApprove: String = "<b>Заявка успешно прошла модерацию и уже отправлена игрокам! 🎉</b>",
                    val onDeny: String = "<b>К сожалению, ваша заявка не прошла модерацию. <tg-emoji emoji-id=\"5197279271361456513\">😞</tg-emoji></b>\nСоветуем ещё раз ознакомиться с <a href=\"https://teletype.in/@zixamc/rules-general\">правилами сервера</a>.\n\nХотите создать новую заявку? <tg-emoji emoji-id=\"5278747228939236605\">🤔</tg-emoji>",
                    val onRestrict: String = "<b>Вы были ограничены в взаимодействии с нашим сервером ⛔</b>\nВы больше не сможете создавать и отправлять заявки.",
                    val onAccept: String = "<b>Добро пожаловать на сервер! <tg-emoji emoji-id=\"5208541126583136130\">🎉</tg-emoji></b>",
                    val onReject: String = "К сожалению, ваша заявка была отклонена. <tg-emoji emoji-id=\"5197279271361456513\">😞</tg-emoji>",
                    val onCanceled: String = "Вы отменили свою заявку. Хотите создать новую? <tg-emoji emoji-id=\"5278747228939236605\">🤔</tg-emoji>",
                    val onKick: String = "<b>К сожалению, вы были кикнуты с сервера. <tg-emoji emoji-id=\"5454350746407419714\">❌</tg-emoji></b>",
                    val onLeave: String = "Вы покинули наш сервер, но всегда сможете вернуться! 🫂",
                    val onReturn: String = "<b>С возвращением на сервер! <tg-emoji emoji-id=\"5462910521739063094\">👋</tg-emoji><b>",
                    val onRulesUpdated: String = "Правила сервера были обновлены!\n» <a href=\"https://teletype.in/@zixamc/rules-general\">Ознакомиться</a>\n\n<blockquote><u>Игрок может считаться игроком</u> только до тех пор, <u>пока он согласен с правилами</u> и соблюдает их.</blockquote>",
                )
                data class RequestsBotForUserLangCreating (
                    val needAgreeWithRules: String = "Для начала, необходимо ознакомиться и согласиться с <a href=\"https://teletype.in/@zixamc/rules-general\">правилами сервера</a>.\n<blockquote>Соглашаясь с настоящими правилами членства в составе игроков, вы также <i>соглашаетесь с остальными правилами</i> сервера, обязуясь соблюдать их. <b>Незнание правил не освобождает от ответственности!</b></blockquote>",
                    val mustAgreeWithRules: String = "Для продолжения вы должны ознакомиться и согласиться с правилами сервера!",
                    val needNickname: String = "Отлично! Придумайте себе никнейм для игры на сервере:",
                    val wrongNickname: String = "Никнейм должен быть от 3 до 16 символов и содержать только символы a-z, A-Z, 0-9 и _",
                    val takenNickname: String = "Такой никнейм уже занят!",
                    val needRequestText: String = "Замечательно, настало время написать свою заявку. <tg-emoji emoji-id=\"5334882760735598374\">📝</tg-emoji>\n\nВ заявке вы должны описать себя, свой опыт игры в Minecraft и почему вы захотели стать игроком нашего сервера. Постарайтесь отвечать развёрнуто, что бы мы могли оценить вас.\nВы должны оформить свою заявку согласно <a href=\"https://teletype.in/@zixamc/rules-requests\">правилам создания и рассмотрения заявок</a>.",
                    val confirmSendRequest: String = "Всё готово! Осталось только отправить заявку. <tg-emoji emoji-id=\"5406901223326495466\">🖥</tg-emoji>\nДля отправки заявки нажмите на кнопку <tg-emoji emoji-id=\"5197474438970363734\">⤵️</tg-emoji>",
                    val youAreNowCreatingRequest: String = "В данный момент, вы уже пишете заявку, хотите начать сначала? <tg-emoji emoji-id=\"5278747228939236605\">🤔</tg-emoji>",
                    val youHavePendingRequest: String = "Вы уже имеете заявку на рассмотрении! <tg-emoji emoji-id=\"538219493505737293\">⏱</tg-emoji>",
                    val doYouWantToCancelRequest: String = "Вы хотите отменить свою заявку? <tg-emoji emoji-id=\"5445267414562389170\">🗑</tg-emoji>",
                    val youAreNowPlayer: String = "Вы уже игрок сервера! <tg-emoji emoji-id=\"5429579672851596232\">😏</tg-emoji>",
                )
            }
        }
        data class RequestsBotForTarget (
            val chatId: Long = 0,
            val topicId: Int = 0,
            val lang: RequestsBotForTargetLang = RequestsBotForTargetLang(),
        ) {
            data class RequestsBotForTargetLang (
                val event: RequestsBotForTargetLangEvents = RequestsBotForTargetLangEvents(),
                val poll: RequestsBotForTargetLangPoll = RequestsBotForTargetLangPoll(),
            ) {
                data class RequestsBotForTargetLangEvents (
                    val onSend: String = "<b>Внимание, новая заявка! <tg-emoji emoji-id=\"5220214598585568818\">🚨</tg-emoji></b>\n\nЧто бы задать вопрос заявителю, ответьте на заявку или на сообщение, отвечающее на заявку.\n\n<b>{mentionAll}</b>",
                    val onCanceled: String = "<b>Заявка была отменена заявителем. <tg-emoji emoji-id=\"5210952531676504517\">❌</tg-emoji></b>",
                    val onAccept: String = "<b>{nickname} теперь игрок сервера! <tg-emoji emoji-id=\"5217608395250485583\">🕺</tg-emoji></b>",
                    val onReject: String = "<b>Заявка {nickname} была отклонена! <tg-emoji emoji-id=\"5210952531676504517\">❌</tg-emoji></b>",
                    val onRulesUpdated: String = "Правила сервера были обновлены!\n» <a href=\"https://teletype.in/@zixamc/rules-general\">Ознакомиться</a>\n\n<blockquote><u>Игрок может считаться игроком</u> только до тех пор, <u>пока он согласен с правилами</u> и соблюдает их.</blockquote>\n\n{mentionAll}",
                    val onPromote: String = "Тип пользователя успешно изменён!",
                    val onKick: String = "<b>{nickname} был кикнут с сервера. <tg-emoji emoji-id=\"5454350746407419714\">❌</tg-emoji></b>",
                    val onRestrict: String = "<b>Пользователь {nickname} был ограничен в взаимодействии с сервером ⛔</b>",
                    val onLeave: String = "<b>{nickname} решил уйти с сервера. <tg-emoji emoji-id=\"5454350746407419714\">❌</tg-emoji></b>",
                    val onReturn: String = "<b>{nickname} вернулся на сервер! <tg-emoji emoji-id=\"5462910521739063094\">👋</tg-emoji></b>",
                )
                data class RequestsBotForTargetLangPoll (
                    val question: String = "Добавлять {nickname} на сервер?",
                    val answerTrue: String = "✅ Да",
                    val answerNull: String = "💤 Не знаю",
                    val answerFalse: String = "⛔ Нет",
                )
            }
        }
        data class RequestsBotForModerator (
            val chatId: Long = 0,
            val topicId: Int = 0,
            val lang: RequestsBotForModeratorLang = RequestsBotForModeratorLang(),
        ) {
            data class RequestsBotForModeratorLang (
                val button: RequestsBotForModeratorLangButtons = RequestsBotForModeratorLangButtons(),
                val event: RequestsBotForModeratorLangEvents = RequestsBotForModeratorLangEvents(),
            ) {
                data class RequestsBotForModeratorLangButtons (
                    val approveSending: String = "Одобрить ✅",
                    val denySending: String = "Отклонить ❌",
                    val restrictSender: String = "Заблокировать заявителя ⛔",
                    val closeRequestVote: String = "Подвести итоги голосования 🗳️",
                )
                data class RequestsBotForModeratorLangEvents (
                    val onNew: String = "<b>Новая заявка от {nickname}!</b>\n\nВнимательно ознакомьтесь с заявкой и проверьте, соответствует ли она <a href=\"https://teletype.in/@zixamc/rules-general\">правилам сервера</a>. \nРазрешите отправку заявки игрокам только в том случае, если соблюдены все правила и критерии.\nПри необходимости, вы можете заблокировать этого заявителя (крайняя мера).",
                    val onApprove: String = "<b>Заявка {nickname} была одобрена и уже отправлена в группу игроков!</b>",
                    val onDeny: String = "<b>Заявка {nickname} была отклонена.</b>",
                    val onCancel: String = "<b>{nickname} отменил(а) свою заявку.</b>",
                    val onVoteClosed: String = "<b>Голосование за добавление {nickname} в состав игроков сервера было закрыто.</b>",
                    val onUserRestricted: String = "<b>Заявитель {nickname} был заблокирован.</b>",
                )
            }
        }
        data class RequestsBotCommonLang (
            val command: RequestsBotTextCommandsDataClass = RequestsBotTextCommandsDataClass(),
            val areYouSureRevokeAgreeWithRules: String = "<b>{nickname}, вы уверены, что хотите отозвать согласие с правилами?</b> 🤔\n<blockquote></blockquote>",
            val thatButtonFor: String = "Эта кнопка предназначалась для {nickname}.",
        ) {
            data class RequestsBotTextCommandsDataClass (
                val acceptHelp: String = "Для того, что бы принять или отклонить заявку, ваше сообщение должно отвечать на заявку или на сообщение, отвечающее на заявку.",
                val rejectHelp: String = "Для того, что бы принять или отклонить заявку, ваше сообщение должно отвечать на заявку или на сообщение, отвечающее на заявку.",
                val promoteHelp: String = "Использование:\n/promote {user_id\nickname} {account_type/account_type_id}\n/promote {account_type/account_type_id} (при ответе на сообщение)\n\nПримеры:\n/promote PulpiLegend Admin\n/promote 0 (ответ на сообщение)",
                val kickHelp: String = "Использование:\n/kick {user_id\nickname}\n/kick (при ответе на сообщение)\n\nПримеры:\n/kick Kleverar\n/kick (ответ на сообщение)",
                val restrictHelp: String = "Использование:\n/restrict {user_id/nickname}\n/restrict (при ответе на сообщение)\n\nПримеры:\n/restrict Kleverar\n/restrict (ответ на сообщение)",
                val leaveHelp: String = "Использование:\n/leave {user_id/nickname}\n/leave (при ответе на сообщение)\n\nПримеры:\n/leave Kleverar\n/leave (ответ на сообщение)",
                val returnHelp: String = "Использование:\n/return {user_id/nickname}\n/return (при ответе на сообщение)\n\nПримеры:\n/return Kleverar\n/return (ответ на сообщение)",
                val permissionDenied: String = "Недостаточно прав для выполнения этой команды!",
            )
        }
    }
    data class ServerBotDataClass (
        val isEnabled: Boolean = true,
        val botToken: String = "",
        val botAPIURL: String = "https://api.telegram.org",
        val pollTimeout: Int = 60,
        val mentionAllReplaceWith: String = "▌",
        val chatSync: ServerBotChatSyncDataClass = ServerBotChatSyncDataClass(),
        val easyAuth: ServerBotEasyAuth = ServerBotEasyAuth(),
        val integration: ServerBotIntegration = ServerBotIntegration(),
    ) {
        data class ServerBotChatSyncDataClass (
            val isEnabled: Boolean = true,
            val defaultGroup: DefaultGroup = DefaultGroup(),
            val messages: ServerBotChatSyncMessageDataClass = ServerBotChatSyncMessageDataClass(),
            val events: ServerBotChatSyncGameEventsDataClass = ServerBotChatSyncGameEventsDataClass(),
            val lang: ServerBotChatSyncLangDataClass = ServerBotChatSyncLangDataClass(),
            val betaMarkdown: Boolean = false,
            val addPrefixToChatMessages: Boolean = true,
            val reply: ChatSyncReply = ChatSyncReply(),
        ) {
            data class DefaultGroup(
                val chatId: Long = 0,
                val topicId: Int? = 0,
                val name: String = "zixa",
                val aliases: List<String> = listOf("main"),
                val prefix: TextData = TextData("<color:aqua>Telegram</color:aqua>"),
                val fromMcPrefix: TextData = TextData("<color:dark_green>Minecraft</color:dark_green>"),
            )
            data class ServerBotChatSyncLangDataClass (
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
//                    val topic: TextData = TextData("<color:dark_aqua><hover:show_text:'Написать в топик'><click:suggest_command:'/r {group} {topicId} '>{topicName} »</click></hover></color:dark_aqua>"),
                )
            }
            data class ServerBotChatSyncMessageDataClass (
                val bluemapUrl: String? = null,
                val requirePrefixInMinecraft: String? = "",
                val keepPrefix: Boolean = false,
                val mergeWindow: Int? = 0,
                val replyInDifferentLine: Boolean = false,
                val styledTelegramMessagesInMinecraft: Boolean = true,
                val parseMarkdownInMinecraftToTelegramMessages: Boolean = true,
            )
            data class ServerBotChatSyncGameEventsDataClass (
                val advancementMessages: ServerBotChatSyncAdvancementsDataClass = ServerBotChatSyncAdvancementsDataClass(),
                val enableDeathMessages: Boolean = true,
                val enableJoinMessages: Boolean = true,
                val enableLeaveMessages: Boolean = true,
                val leaveJoinMergeWindow: Int? = 0,
            ) {
                data class ServerBotChatSyncAdvancementsDataClass(
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
        }
        data class ServerBotIntegration(
            val messageNotPlayer: String = "<b>👋 Приветствую!</b>\nЯ бот приватного Minecraft сервера Zixa City.\n\n<i>Вы не являетесь игроком сервера</i> »\n<b>Подать заявку</b> -> @ZixaMC_request_bot",
            val messageMenu: String = "<b>👋 Приветствую!</b>\nЯ бот приватного Minecraft сервера Zixa City, чем могу помочь?",
            val buttonBackToMenu: String = "« Вернуться в меню",
            val infoButton: String = "Информация о сервере 📌",
            val addToGroupButton: String = "Добавить бота в группу 🎊",
            val audioPlayer: ServerBotAudioPlayer = ServerBotAudioPlayer(),
            val fabricTailor: ServerBotFabricTailor = ServerBotFabricTailor(),
            val group: ServerBotGroupConfig = ServerBotGroupConfig(),
        ) {
            data class ServerBotGroupConfig(
                val sorryOnlyForPlayer: String = "<b>Извините, но добавлять меня в группы могут только игроки</b> 😔\n<blockquote><b>Добавлять бота в группы могут только игроки.</b> Бот может <u>находиться в группе</u> до тех пор, <u>пока среди её членов есть хотя-бы один игрок</u>.</blockquote>\n» <a href=\"https://teletype.in/@zixamc/rules-bot\">Подробнее</a>",
                val hasNoMorePlayers: String = "<b>Сожалею, но мне придётся покинуть вас</b> 😔\nПохоже, что этой группе не осталось ни одного игрока. Я был рад иметь с вами дело, до скорых встреч!",
                val protectedWasDeleted: String = "<b>Привет новичкам! 👋</b>\nМы очень рады видеть тебя здесь! Советую ознакомиться с правилами приватного Minecraft SMP сервера Zixa City, которым следует эта группа.\n<i>Секретная информация, которую пользоваель не должен был видеть, была удалена, если таковая содержалась в этой группе.</i>",
                val hello: String = "Привет! Я — бот приватного Minecraft SMP сервера Zixa City!\nЯ очень рад, что вы добавили меня, давайте начнём!",
                val needAgreeWithRules: String = "Для продолжения использования этого бота, владелец чата должен ознакомиться и согласиться с правилами приватного Minecraft SMP сервера Zixa City.\n» <a href=\"https://teletype.in/@zixamc/rules-bot\">Ознакомиться</a>\n\n<blockquote>Для работы бота в группе, её <u>создатель должен согласиться с настоящими правилами</u>. После согласия с правилами сервера, <i>на группу распространяются <u>правила общения</u> и настоящие правила использования нашего Telegram бота.</i> Ведя диалог в такой группе, её члены обязуются соблюдать данные правила.</blockquote>",
                val agreeWithRules: String = "С правилами ознакомлен и согласен ✅",
                val needAgreeOwner: String = "Подтвердить согласие должен владелец чата!",
                val needAgreeOwnerOrAdmin: String = "Это действие доступно только админам чата!",
                val haveNotPermission: String = "Это действие доступно только {placeholder} чата",
                val groupHasNoOnlyPlayers: String = "Похоже, что в этой группе есть не только игроки 🤔\n<i>Некоторые функции бота ограничены в целях сохранения секретной информации в тайне.\nЕсли в этой группе только игроки и группа приватная — просим всех её членов написать хотя-бы одно сообщение.</i>",
                val switchToPublic: String = "<b>Эта группа публичная!</b> ⚠️\nНекоторые функции бота ограничены в целях сохранения секретной информации в тайне.",
                val featureDontAvailable: String = "Извините, но эта функция недоступна в вашей группе 😔",
                val restrict: String = "<b>Группа заблокирована!</b> ❌\nВ связи с нарушением правил использования, вы больше не сможете воспользоваться ботом в этой группе.",
                val thinkOfName: String = "Отлично! Теперь, придумайте <u>уникальное короткое имя</u> для этой группы. 📋\n<i>Длина имени: не более 16 символов.\nДопустимые символы: буквы русского и английского алфавита, цифры, знак \"-\" и нижнее подчёркивание.</i>\nАдминистратор может выбрать одно имя из списка, или написать другое, ответив на это сообщение.",
                val incorrectName: String = "Ошибка! Имя должно иметь длину не более 16 символов и состоять только из допустимых символов.",
                val nameIsTaken: String = "Ошибка! Это имя уже занято.",
                val done: String = "<b>Замечательно! Давайте перейдём к настройке моих функций.</b> 🛠️",
                val selectFeature: String = "<b>Вот список всех функций, доступных в вашей группе.</b> 🔮",
                val selectTopicForFeature: String = "<b>Выберите топик группы, для которого вы хотите применить эту функцию.</b> 🔮\nДля этого отправьте команду /selectTopic в нужный вам топик в этой группе.\n<i><u>Выбирая главный топик, вы синхронизируете с сервером всю группу!</u></i>",
                val rulesUpdated: String = "Правила сервера были обновлены!\n» <a href=\"https://teletype.in/@zixamc/rules-bot\">Ознакомиться</a>\n\n<blockquote>Бот может работать в группе только до тех пор, пока подтверждено согласие с правилами и среди членов группы есть хотя-бы один игрок.</blockquote>",
                val removeAgreeWithRulesAreYouSure: String = "<b>Вы уверены, что хотите отозвать своё согласие?</b> 🤔\nЭтот бот может работать в вашей группе только до тех пор, пока вы согласны с правилами сервера и придерживаетесь их.",
                val removeAgreeWithRules: String = "Отозвать согласие ❌",
                val confirm: String = "Подтвердить ✅",
                val cancelConfirm: String = "Отмена ❌",
                val success: String = "Готово ✅",
                val backButton: String = "« Назад",
                val confirmSetUpFeature: String = "Установить ✅",
                val wait: String = "<b>Пожалуйста, подождите...</b> ⌚\nЭто действие может занять некоторое время.",
                val features: Features = Features(),
                val settings: Settings = Settings(),
                val memberStatus: MemberStatus = MemberStatus(),
            ) {
                data class Features(
                    val chatSync: ChatSync = ChatSync(),
                    val console: Console = Console(),
                    val playersGroup: PlayersGroup = PlayersGroup(),
                ) {
                    data class ChatSync(
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
                    data class Console(
                        val display: String = "Консоль 💻",
                        val description: String = "<b>Вы можете связать эту группу с консолью Minecraft сервера! 💻</b>\nВы сможете видеть всё сообщения из консоли в этой группе, а также сможете выполнять команды сервера.\nВы можете изменить параметры функции в настройках » /settings",
                        val doneTopic: String = "<b>Готово!</b> 🎉\nТеперь этот топик синхронизирован с консолью сервера!\nВы можете изменить параметры функции в настройках » /settings",
                        val doneNoTopic: String = "<b>Готово!</b> 🎉\nТеперь эта группа синхронизирована с консолью сервера!",
                        val options: String = "» ID топика -> <code>{topicId}</code>",
                        val newSession: String = "<b>Стартовала новая сессия!</b> ✅",
                        val stopSession: String = "<b>Сессия завершена</b> ❌",
                    )
                    data class PlayersGroup(
                        val display: String = "Группа игроков 🎮",
                        val description: String = "<b>Вы можете сделать группу доступной для игроков</b> 🎮\nЭта функция позволит автоматически добавлять в группу игроков, если в этой группе включены заявки на вступление. Также, настроив определённый параметр, бот будет автоматически удалять тех, кто больше не является игроком.\nОбратите внимание, что <u>функция будет работать только если бот является администратором</u> в этой группе.",
                        val done: String = "<b>Готово!</b> 🎉\nТеперь в этой группе включена функция <b>Группа игроков 🎮</b>!\nВы сможете настроить её в настройках » /settings",
                        val options: String = "» Авто-одобрение входа игроков -> <code>{autoAccept}</code>\n» Авто-удаление не игроков -> <code>{autoRemove}</code>",
                        val autoAccept: String = "Авто-добавление игроков 👋",
                        val autoRemove: String = "Авто-удаление не игроков 🚫",
                    )
                }
                data class Settings(
                    val text: String = "<b>Настройки группы</b> 🛠️\nИмя группы: {groupName}",
                    val features: String = "Функции бота 🔮",
                    val featuresDescription: String = "<b>Активные функции бота</b>",
                    val featureDescription: String = "<b>{feature}</b>\n\n{options}",
                    val editFeature: String = "Изменить параметры ✏️",
                    val removeFeature: String = "Удалить функцию ❌",
                    val featureRemoved: String = "Функция <b>{feature}</b> была удалёна из группы ❌",
                    val addFeature: String = "➕ Добавить функцию",
                    val changeName: String = "Изменить имя группы 📋",
                    val aliases: String = "Псевдонимы группы 📃",
                    val aliasesDescription: String = "Псевдонимы позволяют добавлять группе дополнительные уникальные имена, с помощью которых можно обращаться к группе также, как и с обычным именем <i>(на пример, использовать в команде /r)</i>",
                    val removeAlias: String = "{alias} (Удалить)",
                    val aliasDeleted: String = "Псевдоним <u>{alias}</u> был удалён.",
                    val addAlias: String = "➕ Добавить псевдоним",
                    val nullPlaceholder: String = "Не установлено",
                    val nullTopicPlaceholder: String = "Вся группа",
                    val truePlaceholder: String = "Включено",
                    val falsePlaceholder: String = "Выключено",
                    val turnOn: String = "Включить ✅",
                    val turnOff: String = "Выключить ❌",
                    val selectTopic: String = "Изменить топик 📌"
                )
                data class MemberStatus(
                    val creator: String = "создателю",
                    val administrators: String = "администраторам",
                    val members: String = "членам",
                )
            }
            data class ServerBotAudioPlayer(
                val modIsNodInstalled: String = "Похоже, AudioPlayer не установлен на сервере...",
                val buttonMenu: String = "Загрузить аудио в AudioPlayer 🎧",
                val messageUpload: String = "Отправьте аудио в этот чат.\nРазмер аудио не должен превышать 20МБ.",
                val messageErrorUpload: String = "Ошибка! Размер аудио не должен превышать 20МБ.",
                val messageIncorrectExtension: String = "Ошибка! Аудио должно иметь расширение \".mp3\" или \".wav\". Вы можете воспользоваться онлайн-конвертером для изменения формата.",
                val messageDone: String = "<b>Аудио успешно загружено на сервер!</b>\nТеперь вы можете использовать его в AudioPlayer.\n\n<b>UUID аудио »</b>\n<code>{filename}</code>\n\n<i>Что бы записать аудио на предмет, возьмите его в руку и выполните команду</i> ->\n<code>/audioplayer apply {filename}</code>",
                val messagePreparing: String = "<b>Пожалуйста, подождите...</b>"
            )
            data class ServerBotFabricTailor(
                val buttonMenu: String = "Загрузить скин на сервер 👘",
                val messageUploadPlayer: String = "Выберите аккаунт, для которого вы хотите загрузить скин.",
                val messageUploadFile: String = "Отправьте файл скина в этот чат.\nОбратите внимание, вы должны <u>отправить изображение как файл</u> (без сжатия).",
                val messageUploadModel: String = "Выберите модель загружаемого скина:\n- Classic — обычный скин,\n- Slim — скин с тонкими руками.",
                val buttonModelClassic: String = "Classic",
                val buttonModelSlim: String = "Slim",
                val messageErrorUpload: String = "Ошибка загрузки изображения скина! Обратитесь за помощью к администратору.",
                val messageErrorSet: String = "Ошибка установки скина на сервере!\nОшибка могла возникнуть, если вы ни разу не заходили на сервер с вашим никнеймом. Попробуйте зайти на сервер и попробовать снова. Если ошибка осталась, обратитесь за помощью к администратору.",
                val messageErrorNotOnline: String = "Ошибка установки скина! Вы должны находиться онлайн (в игре) на сервере!",
                val messageErrorNotAnImage: String = "Ошибка! Вы должны <u>отправить изображение как файл</u> (без сжатия).",
                val messageErrorWrongResolution: String = "Ошибка! Вы должны отправить изображение с разрешением 64x64 (стандартный скин) или 64x32 (устаревший скин).",
                val messageDone: String = "Скин успешно установлен!",
                val messagePreparing: String = "<b>Пожалуйста, подождите...</b>"
            )
        }
        data class ServerBotEasyAuth (
            val isEnabled: Boolean = true,
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
        }
    }
    data class TempConfig (
        var isSilentRestart: Boolean = false,
    )
}