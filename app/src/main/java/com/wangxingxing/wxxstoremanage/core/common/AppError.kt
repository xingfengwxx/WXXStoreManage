package com.wangxingxing.wxxstoremanage.core.common

enum class AppErrorType {
    NetworkUnavailable,
    SessionExpired,
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
