package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

open class TgReplyMarkup

data class TgInlineKeyboardMarkup(
    val inline_keyboard: List<List<TgInlineKeyboardButton>>
) : TgReplyMarkup() {
    data class TgInlineKeyboardButton(
        val text: String,
        val url: String? = null,
        val callback_data: String? = null,
        //val web_app: TgWebAppInfo? = null,
        //val login_url: TgLoginUrl? = null,
        val switch_inline_query: String? = null,
        val switch_inline_query_current_chat: String? = null,
        //val switch_inline_query_chosen_chat: TgSwitchInlineQueryChosenChat? = null,
        val copy_text: TgCopyTextButton? = null,
    ) {
        data class TgCopyTextButton(
            val text: String,
        )
    }
}
data class TgReplyKeyboardMarkup(
    val keyboard: List<TgKeyboardButton>,
) : TgReplyMarkup() {
    data class TgKeyboardButton(
        val text: String,
        val request_users: TgKeyboardButtonRequestUsers? = null,
        val request_chat: TgKeyboardButtonRequestChat? = null,
        val request_contact: Boolean? = null,
        val request_location: Boolean? = null,
        val request_poll: TgKeyboardButtonPollType? = null,
        //val web_app: TgWebAppInfo? = null,
    ) {
        data class TgKeyboardButtonRequestUsers(
            val request_id: Int,
            val user_is_bot: Boolean? = null,
            val user_is_premium: Boolean? = null,
            val max_quantity: Int? = null,
            val request_name: Boolean? = null,
            val request_username: Boolean? = null,
            val request_photo: Boolean? = null,
        )
        data class TgKeyboardButtonRequestChat(
            val request_id: Int,
            val chat_is_channel: Boolean,
            val chat_is_forum: Boolean? = null,
            val chat_has_username: Boolean? = null,
            val chat_is_created: Boolean? = null,
            val user_administrator_rights: TgChatAdministratorRights? = null,
            val bot_administrator_rights: TgChatAdministratorRights? = null,
            val bot_is_member: Boolean? = null,
            val request_title: Boolean? = null,
            val request_username: Boolean? = null,
            val request_photo: Boolean? = null,
        )
        data class TgKeyboardButtonPollType(
            val type: String? = null,
        )
    }
}
data class TgReplyKeyboardRemove (
    val remove_keyboard: Boolean,
    val selective: Boolean? = null
) : TgReplyMarkup()
data class TgForceReply (
    val force_reply: Boolean,
    val input_field_placeholder: String? = null,
    val selective: Boolean? = null
) : TgReplyMarkup()
