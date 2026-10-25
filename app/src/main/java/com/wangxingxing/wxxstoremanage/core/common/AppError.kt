/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 统一错误类型与错误对象；UI 只消费 AppErrorType，不直接展示异常信息。
 */
package com.wangxingxing.wxxstoremanage.core.common

enum class AppErrorType {
    NetworkUnavailable,
    SessionExpired,
    AuthenticationFailed,
    ServiceUnavailable,
    Validation,
    Conflict,
    NotFound,
    Unknown,
}

data class AppError(
    val type: AppErrorType,
    val cause: Throwable? = null,
    val serverCode: String? = null,
)
