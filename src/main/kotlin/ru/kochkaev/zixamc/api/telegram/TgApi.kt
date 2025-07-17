package ru.kochkaev.zixamc.api.telegram

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url
import ru.kochkaev.zixamc.api.telegram.model.*
import ru.kochkaev.zixamc.api.telegram.request.TgAnswerCallbackQueryRequest
import ru.kochkaev.zixamc.api.telegram.request.TgApproveChatJoinRequest
import ru.kochkaev.zixamc.api.telegram.request.TgBanChatMemberRequest
import ru.kochkaev.zixamc.api.telegram.request.TgDeleteMessageRequest
import ru.kochkaev.zixamc.api.telegram.request.TgDeleteMessagesRequest
import ru.kochkaev.zixamc.api.telegram.request.TgEditMessageReplyMarkupRequest
import ru.kochkaev.zixamc.api.telegram.request.TgEditMessageRequest
import ru.kochkaev.zixamc.api.telegram.request.TgForwardMessageRequest
import ru.kochkaev.zixamc.api.telegram.request.TgGetChatMemberCountRequest
import ru.kochkaev.zixamc.api.telegram.request.TgGetChatMemberRequest
import ru.kochkaev.zixamc.api.telegram.request.TgGetChatRequest
import ru.kochkaev.zixamc.api.telegram.request.TgGetFileRequest
import ru.kochkaev.zixamc.api.telegram.request.TgLeaveChatRequest
import ru.kochkaev.zixamc.api.telegram.request.TgPinChatMessageRequest
import ru.kochkaev.zixamc.api.telegram.request.TgSendMessageRequest
import ru.kochkaev.zixamc.api.telegram.request.TgSendPollRequest
import ru.kochkaev.zixamc.api.telegram.request.TgStopPollRequest
import ru.kochkaev.zixamc.api.telegram.request.TgUnbanChatMemberRequest

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
    @POST("deleteMessages")
    suspend fun deleteMessages(@Body data: TgDeleteMessagesRequest): TgResponse<Boolean>

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