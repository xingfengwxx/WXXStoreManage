/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 提供 Json、OkHttpClient 与两套 Retrofit；debug 下安装脱敏后的完整请求日志，release 不安装。
 */
package com.wangxingxing.wxxstoremanage.core.network.di

import com.wangxingxing.wxxstoremanage.BuildConfig
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.network.api.WanAndroidAuthApi
import com.wangxingxing.wxxstoremanage.core.network.logging.redactSensitiveLog
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WanAndroidRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(logger: AppLogger): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                // 日志仍走 AppLogger；密码、token、Cookie 与 Authorization 在输出前脱敏。
                addInterceptor(
                    HttpLoggingInterceptor { message ->
                        logger.debug(HTTP_LOG_SECTION, redactSensitiveLog(message))
                    }.apply {
                        level = HttpLoggingInterceptor.Level.BODY
                        redactHeader("Cookie")
                        redactHeader("Set-Cookie")
                        redactHeader("Authorization")
                    },
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        // 构建脚本已要求 API_BASE_URL 以 "/" 结尾，否则 Retrofit 初始化时会抛异常。
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    @WanAndroidRetrofit
    fun provideWanAndroidRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.WANANDROID_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideWanAndroidAuthApi(@WanAndroidRetrofit retrofit: Retrofit): WanAndroidAuthApi =
        retrofit.create(WanAndroidAuthApi::class.java)

    private const val HTTP_LOG_SECTION = "http"
}
