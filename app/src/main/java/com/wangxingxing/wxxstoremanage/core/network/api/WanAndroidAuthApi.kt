/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 玩Android 认证接口；登录用 Response 读取 Set-Cookie，登出显式携带会话 Cookie。
 */
package com.wangxingxing.wxxstoremanage.core.network.api

import com.wangxingxing.wxxstoremanage.core.network.dto.ApiEnvelope
import com.wangxingxing.wxxstoremanage.core.network.dto.LoginResponseDto
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface WanAndroidAuthApi {

    @FormUrlEncoded
    @POST("user/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): Response<ApiEnvelope<LoginResponseDto>>

    @GET("user/logout/json")
    suspend fun logout(
        @Header("Cookie") sessionCookie: String,
    ): Response<ApiEnvelope<JsonElement>>
}
