package ru.kochkaev.zixamc.tgbridge.dataclassTelegram

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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

    @POST("approveChatJoinRequest")
    suspend fun approveChatJoinRequest(@Body data: TgApproveChatJoinRequest): TgResponse<Boolean>

    @GET("getUpdates")
    suspend fun getUpdates(
        @Query("offset") offset: Int,
        @Query("timeout") timeout: Int,
        @Query("allowed_updates") allowedUpdates: List<String> = listOf("message"),
    ): TgResponse<List<TgUpdate>>

    @POST("deleteWebhook")
    suspend fun deleteWebhook(): TgResponse<Boolean>
}