/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 登录专用错误映射；注意登录请求的 401 表示本次凭据未被接受，不是已有会话失效。
 */
package com.wangxingxing.wxxstoremanage.core.data.mapper

import com.wangxingxing.wxxstoremanage.core.common.AppError
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

private const val ERROR_CODE_AUTHENTICATION_FAILED = -1
private const val ERROR_CODE_SESSION_EXPIRED = -1001

internal fun Throwable.toLoginAppError(): AppError = when (this) {
    is CancellationException -> throw this
    is HttpException -> AppError(
        type = code().toLoginAppErrorType(),
        cause = this,
    )
    is IOException -> AppError(
        type = AppErrorType.NetworkUnavailable,
        cause = this,
    )
    else -> AppError(
        type = AppErrorType.Unknown,
        cause = this,
    )
}

internal fun loginBusinessError(errorCode: Int): AppError = when (errorCode) {
    ERROR_CODE_AUTHENTICATION_FAILED -> AppError(
        type = AppErrorType.AuthenticationFailed,
        serverCode = errorCode.toString(),
    )
    ERROR_CODE_SESSION_EXPIRED -> AppError(
        type = AppErrorType.SessionExpired,
        serverCode = errorCode.toString(),
    )
    else -> AppError(
        type = AppErrorType.Unknown,
        serverCode = errorCode.toString(),
    )
}

private fun Int.toLoginAppErrorType(): AppErrorType = when (this) {
    400, 422 -> AppErrorType.Validation
    // 全局映射约定 401 为会话失效；登录接口没有既有会话，因此单独映射为认证失败。
    401 -> AppErrorType.AuthenticationFailed
    404 -> AppErrorType.NotFound
    409 -> AppErrorType.Conflict
    in 500..599 -> AppErrorType.ServiceUnavailable
    else -> AppErrorType.Unknown
}
