/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 将异常映射为 AppError；注意 CancellationException 必须原样抛出，避免吞掉协程取消。
 */
package com.wangxingxing.wxxstoremanage.core.data.mapper

import com.wangxingxing.wxxstoremanage.core.common.AppError
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

fun Throwable.toAppError(): AppError = when (this) {
    // 协程取消不是业务错误，必须原样抛出；映射为 Unknown 会吞掉取消并破坏结构化并发。
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
