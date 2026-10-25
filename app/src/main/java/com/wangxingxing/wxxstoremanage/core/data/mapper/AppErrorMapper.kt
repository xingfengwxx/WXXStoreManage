package com.wangxingxing.wxxstoremanage.core.data.mapper

import com.wangxingxing.wxxstoremanage.core.common.AppError
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

fun Throwable.toAppError(): AppError = when (this) {
    is CancellationException -> throw this
    is HttpException -> AppError(
        type = code().toAppErrorType(),
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

private fun Int.toAppErrorType(): AppErrorType = when (this) {
    400, 422 -> AppErrorType.Validation
    401 -> AppErrorType.SessionExpired
    404 -> AppErrorType.NotFound
    409 -> AppErrorType.Conflict
    in 500..599 -> AppErrorType.ServiceUnavailable
    else -> AppErrorType.Unknown
}
