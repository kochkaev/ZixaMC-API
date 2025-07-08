package ru.kochkaev.zixamc.api.config

import net.fabricmc.loader.api.FabricLoader
import java.io.File

/**
 * @author kochkaev
 */
data class Config (
    val general: GeneralConfig = GeneralConfig(),
    val mySQL: ConfigSQL = ConfigSQL(),
    val serverBot: ServerBotDataClass = ServerBotDataClass(),
) {
    data class GeneralConfig (
        val serverIP: String = "",
        val lang: GeneralConfigLang = GeneralConfigLang(),
        val rules: RulesLang = RulesLang(),
        val buttons: ButtonsLang = ButtonsLang(),
    ) {
        data class GeneralConfigLang (
            val infoMessage: String = "<b>Zixa City - Информация о сервере ✨</b>\n\n<i><tg-emoji emoji-id=\"5447410659077661506\">🌐</tg-emoji> IP сервера</i> -> <tg-spoiler>{serverIP}</tg-spoiler>\n<i><tg-emoji emoji-id=\"5415803062738504079\">🗺</tg-emoji> WEB карта</i> -> <a href=\"https://zixamc.dynmap.xyz/\">перейти</a>\n<i><tg-emoji emoji-id=\"5258046117932711905\">📕</tg-emoji> Правила сервера</i> -> <a href=\"https://teletype.in/@zixamc/rules-gaming\">перейти</a>\n<i><tg-emoji emoji-id=\"5284997273938305348\">💎</tg-emoji>Версия Minecraft</i> -> Java Edition 1.21 (Fabric)\n\nРазглашать кому-либо IP сервера или адресс WEB карты категорически <b>Запрещенно!</b>\nЕсли, вдруг, вы случайно где-то спалили это информацию, необходимо сообщить об этом администраторам.\n\n<b>Приятной игры! <tg-emoji emoji-id=\"5465465194056525619\">👍</tg-emoji></b>",
            val buttonCopyServerIP: String = "Копировать IP сервера 📋",
        )
        data class RulesLang(
            val updated4player: String = "Правила сервера были обновлены!\n» <a href=\"https://teletype.in/@zixamc/rules-general\">Ознакомиться</a>\n\n<blockquote><u>Игрок может считаться игроком</u> только до тех пор, <u>пока он согласен с правилами</u> и соблюдает их.</blockquote>\n\n{mentionAll}",
            val confirmRemoveAgree4player: String = "<b>{nickname}, вы уверены, что хотите отозвать согласие с правилами?</b> 🤔\n<blockquote><u>Игрок может считаться игроком</u> только до тех пор, <u>пока он согласен с правилами</u> и соблюдает их.</blockquote>\nПосле того, как вы отзовёте своё согласие, вы перестанете быть игроком нашего сервера.",
            val updated4group: String = "Правила сервера были обновлены!\n» <a href=\"https://teletype.in/@zixamc/rules-bot\">Ознакомиться</a>\n\n<blockquote>Бот может работать в группе только до тех пор, пока подтверждено согласие с правилами и среди членов группы есть хотя-бы один игрок.</blockquote>",
            val confirmRemoveAgree4group: String = "<b>Вы уверены, что хотите отозвать своё согласие?</b> 🤔\nЭтот бот может работать в вашей группе только до тех пор, пока вы согласны с правилами сервера и придерживаетесь их.",
            val thatButtonFor: String = "Эта кнопка предназначалась для {nickname}.",
            val agreeButton: String = "С правилами ознакомлен и согласен ✅",
            val removeButton: String = "Отозвать согласие ❌",
            val onLeave4player: String = "Вы покинули наш сервер, но всегда сможете вернуться! 🫂",
            val onLeave4group: String = "<b>{nickname} решил уйти с сервера. <tg-emoji emoji-id=\"5454350746407419714\">❌</tg-emoji></b>",
        )
        data class ButtonsLang(
            val confirm: String = "Подтвердить ✅",
            val cancel: String = "Отмена ❌",
            val success: String = "Готово ✅",
            val back: String = "« Назад",
        )
    }
    data class ServerBotDataClass (
        val botToken: String = "",
        val botAPIURL: String = "https://api.telegram.org",
        val pollTimeout: Int = 60,
        val mentionAllReplaceWith: String = "▌",
        val menu: MenuConfig = MenuConfig(),
        val adminPanel: AdminPanelConfig = AdminPanelConfig(),
        val group: GroupConfig = GroupConfig(),
    ) {
        data class MenuConfig(
            val messageNotPlayer: String = "<b>👋 Приветствую, {nickname}!</b>\nЯ бот приватного Minecraft сервера Zixa City.\n\n<i>Вы не являетесь игроком сервера</i> »\n<b>Подать заявку</b> -> @ZixaMC_request_bot",
            val messageMenu: String = "<b>👋 Приветствую!</b>\nЯ бот приватного Minecraft сервера Zixa City, чем могу помочь?",
            val buttonBackToMenu: String = "« Вернуться в меню",
            val infoButton: String = "Информация о сервере 📌",
            val addToGroupButton: String = "Добавить бота в группу 🎊",
            val removeAgreedWithRules: String = "Отозвать согласие с правилами ❌",
        )
        data class AdminPanelConfig(
            val messageNotAdmin: String = "<b>Это действие доступно только администраторам сервера! ⛔️</b>",
            val messagePanel: String = "<b>💻 ZixaMC::AdminPanel</b>",
            val buttonBackToPanel: String = "« Вернуться в панель",
            val sendRulesUpdated: String = "Уведомить об обновлении правил 📜",
            val sendRulesUpdatedMessage: String = "<b>Отправить уведомление об обновлении правил сервера всем пользователям и группам.</b>\nНеобходимо <u>сообщить</u> о внесении изменений в правила или также <u>снять все согласия</u>?",
            val sendRulesUpdatedNotRemove: String = "Только сообщить ✅",
            val sendRulesUpdatedRemove: String = "Снять все согласия ❌",
        )
        data class GroupConfig(
            val sorryOnlyForPlayer: String = "<b>Извините, но добавлять меня в группы могут только игроки</b> 😔\n<blockquote><b>Добавлять бота в группы могут только игроки.</b> Бот может <u>находиться в группе</u> до тех пор, <u>пока среди её членов есть хотя-бы один игрок</u>.</blockquote>\n» <a href=\"https://teletype.in/@zixamc/rules-bot\">Подробнее</a>",
            val hasNoMorePlayers: String = "<b>Сожалею, но мне придётся покинуть вас</b> 😔\nПохоже, что этой группе не осталось ни одного игрока. Я был рад иметь с вами дело, до скорых встреч!",
            val protectedWasDeleted: String = "<b>Привет новичкам! 👋</b>\nМы очень рады видеть тебя здесь! Советую ознакомиться с правилами приватного Minecraft SMP сервера Zixa City, которым следует эта группа.\n<i>Секретная информация, которую пользоваель не должен был видеть, была удалена, если таковая содержалась в этой группе.</i>",
            val hello: String = "Привет! Я — бот приватного Minecraft SMP сервера Zixa City!\nЯ очень рад, что вы добавили меня, давайте начнём!",
            val needAgreeWithRules: String = "Для продолжения использования этого бота, владелец чата должен ознакомиться и согласиться с правилами приватного Minecraft SMP сервера Zixa City.\n» <a href=\"https://teletype.in/@zixamc/rules-bot\">Ознакомиться</a>\n\n<blockquote>Для работы бота в группе, её <u>создатель должен согласиться с настоящими правилами</u>. После согласия с правилами сервера, <i>на группу распространяются <u>правила общения</u> и настоящие правила использования нашего Telegram бота.</i> Ведя диалог в такой группе, её члены обязуются соблюдать данные правила.</blockquote>",
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
            val confirmSetUpFeature: String = "Установить ✅",
            val wait: String = "<b>Пожалуйста, подождите...</b> ⌚\nЭто действие может занять некоторое время.",
            val features: Features = Features(),
            val settings: Settings = Settings(),
            val memberStatus: MemberStatus = MemberStatus(),
        ) {
            data class Features(
                val playersGroup: PlayersGroup = PlayersGroup(),
            ) {
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
                val selectTopic: String = "Изменить топик 📌",
                val removeAgreedWithRules: String = "Отозвать согласие с правилами ❌",
            )
            data class MemberStatus(
                val creator: String = "создателю",
                val administrators: String = "администраторам",
                val members: String = "членам",
            )
        }
    }
    companion object: ConfigFile<Config>(
        file = File(FabricLoader.getInstance().configDir.toFile(), "ZixaMC-API.json"),
        model = Config::class.java,
        supplier = ::Config
    )
}