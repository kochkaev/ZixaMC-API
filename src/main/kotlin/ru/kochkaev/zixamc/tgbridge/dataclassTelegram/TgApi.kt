package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * @author vanutp
 */
interface TgApi {
    @GET("getMe")
    suspend fun getMe(): TgResponse<TgUser>

    @POST("sendMessage")
    suspend fun sendMessage(@Body data: TgSendMessageRequest): TgResponse<TgMessage>

    @POST("pinChatMessage")
    suspend fun pinMessage(@Body data: TgPinChatMessageRequest): TgResponse<Boolean>

    @POST("sendPoll")
    suspend fun sendPoll(@Body data: TgSendPollRequest): TgResponse<TgMessage>

    @POST("stopPoll")
    suspend fun stopPoll(@Body data: TgStopPollRequest): TgResponse<TgPoll>

    @POST("forwardMessage")
    suspend fun forwardMessage(@Body data: TgForwardMessageRequest): TgResponse<TgMessage>

    @POST("editMessageText")
    suspend fun editMessageText(@Body data: TgEditMessageRequest): TgResponse<TgMessage>

    @POST("editMessageReplyMarkup")
    suspend fun editMessageReplyMarkup(@Body data: TgEditMessageReplyMarkupRequest): TgResponse<TgMessage>

    @POST("deleteMessage")
    suspend fun deleteMessage(@Body data: TgDeleteMessageRequest): TgResponse<Boolean>

    @POST("banChatMember")
    suspend fun banChatMember(@Body data: TgBanChatMemberRequest): TgResponse<Boolean>

    @POST("unbanChatMember")
    suspend fun unbanChatMember(@Body data: TgUnbanChatMemberRequest): TgResponse<Boolean>

    @POST("getChatMember")
    suspend fun getChatMember(@Body data: TgGetChatMemberRequest): TgResponse<TgChatMember>

    @POST("getChatMemberCount")
    suspend fun getChatMemberCount(@Body data: TgGetChatMemberCountRequest): TgResponse<Int>

    @POST("approveChatJoinRequest")
    suspend fun approveChatJoinRequest(@Body data: TgApproveChatJoinRequest): TgResponse<Boolean>

    @POST("leaveChat")
    suspend fun leaveChat(@Body data: TgLeaveChatRequest): TgResponse<Boolean>

    @POST("getChat")
    suspend fun getChat(@Body data: TgGetChatRequest): TgResponse<TgChatFullInfo>

    @POST("answerCallbackQuery")
    suspend fun answerCallbackQuery(@Body data: TgAnswerCallbackQueryRequest): TgResponse<Boolean>

    @POST("getFile")
    suspend fun getFile(@Body data: TgGetFileRequest): TgResponse<TgFile>

    @Streaming
    @GET
    suspend fun downloadFile(@Url fileUrl:String): Response<ResponseBody>

    @GET("getUpdates")
    suspend fun getUpdates(
        @Query("offset") offset: Int,
        @Query("timeout") timeout: Int,
        @Query("allowed_updates") allowedUpdates: List<String> = listOf("message"),
    ): TgResponse<List<TgUpdate>>

    @POST("deleteWebhook")
    suspend fun deleteWebhook(): TgResponse<Boolean>
}