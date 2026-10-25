/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 将 AppErrorType 映射为字符串资源，禁止向用户展示原始异常信息。
 */
package com.wangxingxing.wxxstoremanage.core.ui

import androidx.annotation.StringRes
import com.wangxingxing.wxxstoremanage.R
import com.wangxingxing.wxxstoremanage.core.common.AppErrorType

@StringRes
fun AppErrorType.errorMessageRes(): Int = when (this) {
    AppErrorType.NetworkUnavailable -> R.string.error_network_unavailable
    AppErrorType.SessionExpired -> R.string.error_session_expired
    AppErrorType.ServiceUnavailable -> R.string.error_service_unavailable
    AppErrorType.Validation -> R.string.error_validation
    AppErrorType.Conflict -> R.string.error_conflict
    AppErrorType.NotFound -> R.string.error_not_found
    AppErrorType.Unknown -> R.string.error_unknown
}
