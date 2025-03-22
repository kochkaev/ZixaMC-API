package ru.kochkaev.zixamc.tgbridge.serverBot.group

import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.tgbridge.sql.SQLEntity
import ru.kochkaev.zixamc.tgbridge.sql.SQLGroup
import ru.kochkaev.zixamc.tgbridge.ServerBot.bot
import ru.kochkaev.zixamc.tgbridge.ServerBot.config
import ru.kochkaev.zixamc.tgbridge.chatSync.parser.TextParser
import ru.kochkaev.zixamc.tgbridge.config.TextData
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.*
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.*
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCBHandlerResult.Companion.DELETE_LINKED
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCBHandlerResult.Companion.DELETE_MARKUP
import ru.kochkaev.zixamc.tgbridge.dataclassTelegram.callback.TgCBHandlerResult.Companion.SUCCESS
import ru.kochkaev.zixamc.tgbridge.sql.SQLCallback
import ru.kochkaev.zixamc.tgbridge.sql.SQLProcess
import ru.kochkaev.zixamc.tgbridge.sql.dataclass.FeatureTypes

object ServerBotGroupUpdateManager {

    suspend fun addedToGroup(update: TgChatMemberUpdated) {
        val user = SQLEntity.get(update.from.id)
        if (user == null || !user.accountType.isPlayer()) {
            bot.sendMessage(
                chatId = update.chat.id,
                text = config.integration.group.sorryOnlyForPlayer,
            )
            bot.leaveChat(update.chat.id)
            return
        }
        val group = SQLGroup.getOrCreate(update.chat.id)
        SQLGroup.collectData(update.chat.id, update.from.id)
        if (listOf(TgChatMemberStatuses.LEFT, TgChatMemberStatuses.BANNED).let { it.contains(update.oldChatMember.status) && !it.contains(update.newChatMember.status) }) {
            if (group.isRestricted) {
                bot.sendMessage(
                    chatId = update.chat.id,
                    text = config.integration.group.restrict,
                )
                bot.leaveChat(update.chat.id)
                return
            }
            bot.sendMessage(
                chatId = update.chat.id,
                text = config.integration.group.hello
            )
            if (!group.agreedWithRules) {
                bot.sendMessage(
                    chatId = update.chat.id,
                    text = config.integration.group.needAgreeWithRules,
                    replyMarkup = TgMenu(
                        listOf(
                            listOf(
                                SQLCallback.of(
                                    display = config.integration.group.agreeWithRules,
                                    type = "group",
                                    data = GroupCallback(Operations.AGREE_WITH_RULES)
                                )
                            )
                        )
                    ).inline()
                )
            }
        }
        else if (listOf(TgChatMemberStatuses.LEFT, TgChatMemberStatuses.BANNED).let { !it.contains(update.oldChatMember.status) && it.contains(update.newChatMember.status) }) {
            group.features.setAll(hashMapOf())
            group.agreedWithRules = false
        }
    }

    suspend fun onCallback(cbq: TgCallbackQuery, sql: TgCallback<GroupCallback>): TgCBHandlerResult {
        if (sql.data == null) return SUCCESS
        val group = SQLGroup.get(cbq.message.chat.id) ?: return SUCCESS
        when (sql.data.operation) {
            Operations.AGREE_WITH_RULES -> {
                if (bot.getChatMember(group.chatId, cbq.from.id).status == TgChatMemberStatuses.CREATOR) {
                    group.agreedWithRules = true
                    if (group.name == null) {
                        val callbacks: ArrayList<Long> = arrayListOf()
                        val message = bot.sendMessage(
                            chatId = group.chatId,
                            text = config.integration.group.thinkOfName,
                            replyMarkup = TgMenu(
                                listOf(
                                cbq.message.chat.title.let { escapeName(it) }.let {
                                    if (group.canTakeName(it)) listOf(
                                        SQLCallback.of(
                                            display = it,
                                            type = "group",
                                            data = GroupCallback(
                                                operation = Operations.SET_NAME,
                                                name = it
                                            )
                                        )
                                    ) else listOf()
                                },
                                cbq.message.chat.username?.let { escapeName(it) }?.let {
                                    if (group.canTakeName(it)) listOf(
                                        SQLCallback.of(
                                            display = it,
                                            type = "group",
                                            data = GroupCallback(
                                                operation = Operations.SET_NAME,
                                                name = it
                                            )
                                        )
                                    ) else listOf()
                                } ?: listOf(),
                            )).inlineAndId().also { callbacks.addAll(it.second) } .first
                        )
                        SQLProcess.of(ProcessTypes.GROUP_WAITING_NAME, GroupWaitingNameProcessData(
                            messageId = message.messageId,
                            callbacks = callbacks,
                            nameType = GroupWaitingNameProcessData.NameType.NAME,
                        )).pull(group.chatId)
                    }
                    else sendFeatures(group, cbq.message.messageId, true)
                } else {
                    bot.answerCallbackQuery(
                        callbackQueryId = cbq.id,
                        text = config.integration.group.needAgreeOwner,
                        showAlert = true
                    )
                    return SUCCESS
                }
            }
            Operations.SET_NAME -> {
                if (listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(bot.getChatMember(group.chatId, cbq.from.id).status)) {
                    group.name = sql.data.name
                    SQLProcess.get(group.chatId, ProcessTypes.GROUP_WAITING_NAME)?.drop()
                    sendFeatures(group, cbq.message.messageId, true)
                } else {
                    bot.answerCallbackQuery(
                        callbackQueryId = cbq.id,
                        text = config.integration.group.needAgreeOwnerOrAdmin,
                        showAlert = true
                    )
                    return SUCCESS
                }
            }
            Operations.TOPIC_FEATURE -> {
                bot.editMessageText(
                    chatId = group.chatId,
                    messageId = cbq.message.messageId,
                    text = TextParser.formatLang(
                        text = (FeatureTypes.entries[sql.data.name]?.tgDescription?.invoke()?:"") + "\n\n" + config.integration.group.selectTopicForFeature,
                        "groupName" to group.name!!
                    )
                )
                if (cbq.message.chat.isForum)
                    SQLProcess.of(ProcessTypes.GROUP_SELECT_TOPIC_FEATURE, GroupSelectTopicProcessData(cbq.message.messageId, sql.data.name!!)).pull(group.chatId)
                else {
                    val feature = FeatureTypes.entries[sql.data.name] ?: return DELETE_LINKED
                    group.features.set(feature, feature.getDefault(null))
                    sendNeedPrefix(group, cbq.message.messageId)
                }
//                    bot.editMessageReplyMarkup(
//                        chatId = group.chatId,
//                        messageId = cbq.message.messageId,
//                        replyMarkup = TgMenu(listOf(
//
//                        )).inline()
//                    )
                return DELETE_LINKED
            }
        }
        return DELETE_MARKUP
    }

    suspend fun onMessage(msg: TgMessage) {
        val group = SQLGroup.get(msg.chat.id) ?: return
        val processes = SQLProcess.getAll(group.chatId)
        processes.forEach { process ->
            when (process.type) {
                ProcessTypes.GROUP_WAITING_NAME ->
                    if (msg.replyToMessage?.from?.id == bot.me.id && msg.from != null &&
                        listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(
                            bot.getChatMember(group.chatId, msg.from.id).status
                        )
                    ) {
                        val data = process.data as GroupWaitingNameProcessData
                        val name = msg.effectiveText ?: return
                        if (name.length !in 1..16 || !name.matches(Regex("[а-яa-z0-9_\\-]+", RegexOption.IGNORE_CASE))) {
                            bot.sendMessage(
                                chatId = group.chatId,
                                text = config.integration.group.incorrectName,
                                replyParameters = TgReplyParameters(msg.messageId)
                            )
                        } else if (!group.canTakeName(name)) {
                            bot.sendMessage(
                                chatId = group.chatId,
                                text = config.integration.group.nameIsTaken,
                                replyParameters = TgReplyParameters(msg.messageId)
                            )
                        }
                        else {
                            if (data.nameType == GroupWaitingNameProcessData.NameType.NAME)
                                group.name = name
                            else group.aliases.add(name)
                            bot.editMessageReplyMarkup(
                                chatId = group.chatId,
                                messageId = data.messageId,
                                replyMarkup = TgReplyMarkup(),
                            )
                            data.callbacks.forEach { SQLCallback.get(it)?.drop() }
                            process.drop()
                            sendFeatures(group, msg.messageId, true)
                        }
                    }
                ProcessTypes.GROUP_CHATSYNC_WAITING_PREFIX ->
                    if (msg.replyToMessage?.from?.id == bot.me.id && msg.from != null &&
                        listOf(TgChatMemberStatuses.CREATOR, TgChatMemberStatuses.ADMINISTRATOR).contains(
                            bot.getChatMember(group.chatId, msg.from.id).status
                        )
                    ) {
                        val prefix = msg.effectiveText?:return
                        val mm = TextData(prefix)
                        try {
                            mm.get()
                        } catch (e: Exception) {
                            bot.sendMessage(
                                chatId = group.chatId,
                                text = TextParser.formatLang(
                                    text = config.integration.group.features.chatSync.wrongPrefix,
                                    "error" to e.message.toString()
                                )
                            )
                            return
                        }
                        group.features.getCasted(FeatureTypes.CHAT_SYNC)?.also {
                            it.prefix = mm
                            group.features.set(FeatureTypes.CHAT_SYNC, it)
                        }
                        bot.sendMessage(
                            chatId = group.chatId,
                            text = if (msg.chat.isForum) config.integration.group.features.chatSync.doneTopic
                                else config.integration.group.features.chatSync.doneNoTopic,
                            replyParameters = TgReplyParameters(msg.messageId)
                        )
                    }
            }
        }
    }

    suspend fun selectTopicCommand(msg: TgMessage) {
        val group = SQLGroup.get(msg.chat.id)?:return
        val processes = SQLProcess.getAll(group.chatId)
        processes.forEach { process ->
            when (process.type) {
                ProcessTypes.GROUP_SELECT_TOPIC_FEATURE -> {
                    val data = process.data as GroupSelectTopicProcessData
                    val topicId = msg.messageThreadId
                    val feature = FeatureTypes.entries[data.feature] ?: return
                    group.features.set(feature, feature.getDefault(topicId))
                    process.drop()
                    bot.deleteMessage(group.chatId, data.messageId)
                    sendNeedPrefix(group, topicId)
                }
            }
        }
    }

    suspend fun sendNeedPrefix(group: SQLGroup, replyTo: Int?) {
        bot.sendMessage(
            chatId = group.chatId,
            text = TextParser.formatLang(
                text = config.integration.group.features.chatSync.prefixNeeded,
                "groupName" to group.name.toString(),
            ),
            replyParameters = replyTo?.let { TgReplyParameters(it) }
        )
        SQLProcess.of(ProcessTypes.GROUP_CHATSYNC_WAITING_PREFIX, DummyProcessData()).pull(group.chatId)
    }

    suspend fun sendFeatures(group: SQLGroup, replyTo: Int? = null, withDone: Boolean = false) {
        val message = (if (withDone) config.integration.group.done + "\n" else "") + resolveFeaturesSettingsMessage(group)
        val menu = TgMenu(listOf(
            FeatureTypes.entries
                .map { it.value }
                .filter { it.checkAvailable(group) }
                .map { SQLCallback.of(
                    display = it.tgDisplayName(),
                    type = "group",
                    data = GroupCallback(
                        operation = Operations.TOPIC_FEATURE,
                        name = it.serializedName
                    )
                ) }
        ))
        bot.sendMessage(
            chatId = group.chatId,
            text = message,
            replyParameters = replyTo?.let { TgReplyParameters(it) },
            replyMarkup = menu.inline()
        )
    }

    fun escapeName(current: String) =
        current.replace(" ", "_").replace(Regex("[^а-яa-z0-9_\\-]", RegexOption.IGNORE_CASE), "")
    suspend fun resolveFeaturesSettingsMessage(group: SQLGroup) =
        config.integration.group.groupHasNoOnlyPlayers.let { if (!group.isOnlyPlayers()) it else "" } +
        "\n" + config.integration.group.selectFeature

    data class GroupCallback(
        var operation: Operations,
        var name: String? = null
    ): CallbackData
    enum class Operations {
        @SerializedName("agree_with_rules")
        AGREE_WITH_RULES,
        @SerializedName("set_name")
        SET_NAME,
        @SerializedName("topic_feature")
        TOPIC_FEATURE,
    }
}