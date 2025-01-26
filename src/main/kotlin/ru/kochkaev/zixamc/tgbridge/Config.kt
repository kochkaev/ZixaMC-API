package ru.kochkaev.zixamc.tgbridge

import java.util.*
import net.kyori.adventure.text.format.TextDecoration


/**
 * @author kochkaev
 */
data class Config (
    val general: GeneralConfig = GeneralConfig(),
    val mySQL: MySQLDataClass = MySQLDataClass(),
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
    data class MySQLDataClass (
        val mySQLHost: String = "",
        val mySQLDatabase: String = "",
        val mySQLUser: String = "",
        val mySQLPassword: String = "",
        val mySQLTable: String = "",
    )
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
                    val onRulesUpdated: String = "Правила сервера были обновлены!\n» <a href=\"https://teletype.in/@zixamc/rules-general\">Ознакомиться</a>\n\n<blockquote>Игрокам даётся <u>месяц на ознакомление и подтверждение согласия</u> с обновлёнными правилами. Если <i>по истечении месяца</i> игрок так и не согласился с обновлением правил — <i>он более не может быть игроком</i>.</blockquote>",
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
                    val onRulesUpdated: String = "Правила сервера были обновлены!\n» <a href=\"https://teletype.in/@zixamc/rules-general\">Ознакомиться</a>\n\n<blockquote>Игрокам даётся <u>месяц на ознакомление и подтверждение согласия</u> с обновлёнными правилами. Если <i>по истечении месяца</i> игрок так и не согласился с обновлением правил — <i>он более не может быть игроком</i>.</blockquote>\n\n{mentionAll}",
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
        val targetChatId: Long = 0,
        val targetTopicId: Int = 0,
        val mentionAllReplaceWith: String = "▌",
        val chatSync: ServerBotChatSyncDataClass = ServerBotChatSyncDataClass(),
        val easyAuth: ServerBotEasyAuth = ServerBotEasyAuth(),
    ) {
        data class ServerBotChatSyncDataClass (
            val isEnabled: Boolean = true,
            val chatId: Long = 0,
            val topicId: Int? = 0,
            val messages: ServerBotChatSyncMessageDataClass = ServerBotChatSyncMessageDataClass(),
            val events: ServerBotChatSyncGameEventsDataClass = ServerBotChatSyncGameEventsDataClass(),
            val lang: ServerBotChatSyncLangDataClass = ServerBotChatSyncLangDataClass(),
            val betaMarkdown: Boolean = false,
        ) {
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
                    val advancements: LangAdvancements = LangAdvancements(),
                    val playerList: String = "☝️",
                    val playerListZeroOnline: String = "☝️",
                )

                data class MessageMeta(
                    val messageFormat: String = "[§bTelegram§r] {sender} » {text}",
                    val hoverOpenInTelegram: String = "Открыть в Telegram",
                    val hoverOpenInBrowser: String = "Открыть в браузере",
                    val hoverCopyToClipboard: String = "Копировать в буфер обмена",
                    val hoverTagToReply: String = "Упомянуть его/её",
                    val reply: String = "    ┌──── {sender} » {text}",
                    val replyToMinecraft: String = "    ┌──── {text}",
                    val forward: String = "{from} »",
                    val gif: String = "[GIF]",
                    val document: String = "[Документ]",
                    val photo: String = "[Фото]",
                    val audio: String = "[Аудио]",
                    val sticker: String = "[Стикер]",
                    val video: String = "[Видео]",
                    val videoMessage: String = "[Кружок]",
                    val voiceMessage: String = "[Голосовуха]",
                    val poll: String = "[Опрос: {title}]",
                    val pin: String = "закреплено сообщение »",
                )

                data class MessageFormatting(
                    val linkColor: String = "#FFFF55",
                    val linkFormatting: List<TextDecoration>? = Collections.singletonList(TextDecoration.UNDERLINED),
                    val mentionColor: String = "#FFFF55",
                    val mentionFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val hashtagColor: String = "#FFFF55",
                    val hashtagFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val codeColor: String = "#AAAAAA",
                    val codeFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val spoilerColor: String = "#AAAAAA",
                    val spoilerFormatting: List<TextDecoration>? = Collections.singletonList(TextDecoration.OBFUSCATED),
                    val spoilerReplaceWithChar: String? = "▌",
                    val replyColor: String = "#AAAAAA",
                    val replyFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val forwardColor: String = "#AAAAAA",
                    val forwardFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val mediaColor: String = "#FFFF55",
                    val mediaFormatting: List<TextDecoration>? = Collections.emptyList(),
                    val pinnedMessageColor: String = "#AAAAAA",
                    val pinnedMessageFormatting: List<TextDecoration>? = Collections.emptyList(),
                )

                data class LangMinecraft(
                    val messageMeta: MessageMeta = MessageMeta(),
                    val messageFormatting: MessageFormatting = MessageFormatting(),
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
        }
        data class ServerBotEasyAuth (
            val isEnabled: Boolean = true,
            val langMinecraft: ServerBotEasyAuthLangMinecraft = ServerBotEasyAuthLangMinecraft(),
            val langTelegram: ServerBotEasyAuthLangTelegram = ServerBotEasyAuthLangTelegram(),
        ) {
            data class ServerBotEasyAuthLangMinecraft (
                val onApprove: String = "§aВы были авторизованы через Telegram, хорошей игры!",
                val onDeny: String = "Вход был запрещён в Telegram.",
                val youAreNotPlayer: String = "Вы не являетесь игроком сервера!\nЕсли это ошибка, обратитесь за помощю к администратору.",
                val onJoinTip: String = "Войдите в 1 клик, используя Telegram!",
                val noHaveChatWithBot: String = "§eПохоже, у вас нет диалога с Telegram ботом... \nДля быстрой авторизации на сервере, §nнажмите на это сообщение§r§e, перейдите в чат с ботом и нажмите \"Начать\", после чего, перезайдите на сервер.",
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