/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 玩Android 认证实现；登录成功必须拿到 Set-Cookie 才落盘，登出以清除本机会话为准。
 */
package com.wangxingxing.wxxstoremanage.core.data.repository

import com.wangxingxing.wxxstoremanage.core.common.AppError
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.common.AppResult
import com.wangxingxing.wxxstoremanage.core.data.mapper.loginBusinessError
import com.wangxingxing.wxxstoremanage.core.data.mapper.toCurrentUser
import com.wangxingxing.wxxstoremanage.core.data.mapper.toLoginAppError
import com.wangxingxing.wxxstoremanage.core.data.mapper.toStoredSession
import com.wangxingxing.wxxstoremanage.core.data.session.SessionStore
import com.wangxingxing.wxxstoremanage.core.model.CurrentUser
import com.wangxingxing.wxxstoremanage.core.network.api.WanAndroidAuthApi
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.Headers
import retrofit2.HttpException

class AuthRepositoryImpl @Inject constructor(
    private val api: WanAndroidAuthApi,
    private val sessionStore: SessionStore,
    private val logger: AppLogger,
) : AuthRepository {

    override val session: Flow<CurrentUser?> = sessionStore.session
        .map { storedSession -> storedSession?.toCurrentUser() }

    override suspend fun login(username: String, password: String): AppResult<CurrentUser> {
        val startedAt = System.nanoTime()
        logPhase(ACTION_LOGIN, AppLogPhase.Start)

        val response = try {
            api.login(username, password)
        } catch (cancellation: CancellationException) {
            logCancel(ACTION_LOGIN, startedAt)
            throw cancellation
        } catch (throwable: Throwable) {
            return failure(ACTION_LOGIN, throwable.toLoginAppError(), startedAt)
        }

        if (!response.isSuccessful) {
            return failure(ACTION_LOGIN, HttpException(response).toLoginAppError(), startedAt)
        }
        val envelope = response.body()
            ?: return failure(
                ACTION_LOGIN,
                AppError(AppErrorType.Unknown),
                startedAt,
                mapOf("reason" to "emptyBody"),
            )
        if (envelope.errorCode != SUCCESS_CODE) {
            return failure(
                ACTION_LOGIN,
                loginBusinessError(envelope.errorCode),
                startedAt,
                mapOf("serverCode" to envelope.errorCode.toString()),
            )
        }
        val user = envelope.data?.toCurrentUser()
            ?: return failure(
                ACTION_LOGIN,
                AppError(AppErrorType.Unknown),
                startedAt,
                mapOf("reason" to "missingUser"),
            )
        val sessionCookie = response.headers().sessionCookieOrNull()
            ?: return failure(
                ACTION_LOGIN,
                AppError(AppErrorType.Unknown),
                startedAt,
                mapOf("reason" to "missingSessionCookie"),
            )

        try {
            sessionStore.save(user.toStoredSession(sessionCookie))
        } catch (cancellation: CancellationException) {
            logCancel(ACTION_LOGIN, startedAt)
            throw cancellation
        } catch (throwable: Throwable) {
            return failure(
                ACTION_LOGIN,
                AppError(AppErrorType.Unknown, cause = throwable),
                startedAt,
                mapOf("reason" to "saveSessionFailed"),
            )
        }

        logger.log(
            AppLogEvent(
                feature = FEATURE,
                action = ACTION_LOGIN,
                phase = AppLogPhase.Success,
                durationMs = elapsedMs(startedAt),
            ),
        )
        return AppResult.Success(user)
    }

    override suspend fun logout(): AppResult<Unit> {
        val startedAt = System.nanoTime()
        logPhase(ACTION_LOGOUT, AppLogPhase.Start)

        val storedSession = try {
            sessionStore.read()
        } catch (cancellation: CancellationException) {
            logCancel(ACTION_LOGOUT, startedAt)
            throw cancellation
        } catch (throwable: Throwable) {
            logger.log(
                AppLogEvent(
                    feature = FEATURE,
                    action = ACTION_LOGOUT,
                    phase = AppLogPhase.Failure,
                    context = mapOf("reason" to "readSessionFailed"),
                    throwable = throwable,
                ),
            )
            null
        }

        if (storedSession != null) {
            try {
                val response = api.logout(storedSession.cookie)
                if (!response.isSuccessful) {
                    logger.log(
                        AppLogEvent(
                            feature = FEATURE,
                            action = ACTION_LOGOUT,
                            phase = AppLogPhase.Failure,
                            context = mapOf(
                                "reason" to "serverLogoutHttpError",
                                "status" to response.code().toString(),
                            ),
                            throwable = HttpException(response),
                        ),
                    )
                } else {
                    response.body()?.errorCode?.takeIf { it != SUCCESS_CODE }?.let { serverCode ->
                        logger.log(
                            AppLogEvent(
                                feature = FEATURE,
                                action = ACTION_LOGOUT,
                                phase = AppLogPhase.Failure,
                                context = mapOf(
                                    "reason" to "serverLogoutBusinessError",
                                    "serverCode" to serverCode.toString(),
                                ),
                            ),
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                logCancel(ACTION_LOGOUT, startedAt)
                throw cancellation
            } catch (throwable: Throwable) {
                // 服务端登出是尽力而为，失败不能阻止本机会话清除。
                logger.log(
                    AppLogEvent(
                        feature = FEATURE,
                        action = ACTION_LOGOUT,
                        phase = AppLogPhase.Failure,
                        context = mapOf("reason" to "serverLogoutFailed"),
                        throwable = throwable,
                    ),
                )
            }
        }

        return try {
            sessionStore.clear()
            logger.log(
                AppLogEvent(
                    feature = FEATURE,
                    action = ACTION_LOGOUT,
                    phase = AppLogPhase.Success,
                    durationMs = elapsedMs(startedAt),
                ),
            )
            AppResult.Success(Unit)
        } catch (cancellation: CancellationException) {
            logCancel(ACTION_LOGOUT, startedAt)
            throw cancellation
        } catch (throwable: Throwable) {
            failure(
                ACTION_LOGOUT,
                AppError(AppErrorType.Unknown, cause = throwable),
                startedAt,
                mapOf("reason" to "clearSessionFailed"),
            )
        }
    }

    private fun failure(
        action: String,
        error: AppError,
        startedAt: Long,
        context: Map<String, String> = emptyMap(),
    ): AppResult.Failure {
        val enrichedContext = buildMap {
            putAll(context)
            put("errorType", error.type.name)
            error.serverCode?.let { put("serverCode", it) }
        }
        logger.log(
            AppLogEvent(
                feature = FEATURE,
                action = action,
                phase = AppLogPhase.Failure,
                context = enrichedContext,
                durationMs = elapsedMs(startedAt),
                throwable = error.cause,
            ),
        )
        return AppResult.Failure(error)
    }

    private fun logPhase(action: String, phase: AppLogPhase) {
        logger.log(
            AppLogEvent(
                feature = FEATURE,
                action = action,
                phase = phase,
            ),
        )
    }

    private fun logCancel(action: String, startedAt: Long) {
        logger.log(
            AppLogEvent(
                feature = FEATURE,
                action = action,
                phase = AppLogPhase.Cancel,
                durationMs = elapsedMs(startedAt),
            ),
        )
    }

    private fun Headers.sessionCookieOrNull(): String? =
        values("Set-Cookie")
            .asSequence()
            .map { header -> header.substringBefore(';').trim() }
            .firstOrNull { cookie ->
                cookie.startsWith(JSESSIONID_PREFIX, ignoreCase = true) &&
                    cookie.length > JSESSIONID_PREFIX.length
            }

    private fun elapsedMs(startedAt: Long): Long = (System.nanoTime() - startedAt) / NANOS_PER_MILLI

    private companion object {
        const val FEATURE = "auth"
        const val ACTION_LOGIN = "login"
        const val ACTION_LOGOUT = "logout"
        const val SUCCESS_CODE = 0
        const val JSESSIONID_PREFIX = "JSESSIONID="
        const val NANOS_PER_MILLI = 1_000_000
    }
}
