/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : AppLogger 的 Logcat 实现；按阶段选择日志级别，禁止写入 token、密码和个人信息。
 */
package com.wangxingxing.wxxstoremanage.core.common

import android.util.Log
import javax.inject.Inject

class LogcatAppLogger @Inject constructor() : AppLogger {

    override fun log(event: AppLogEvent) {
        val message = buildString {
            append("feature=").append(event.feature)
            append(" action=").append(event.action)
            append(" phase=").append(event.phase.name)
            event.durationMs?.let { append(" durationMs=").append(it) }
            if (event.context.isNotEmpty()) {
                append(" context=").append(event.context)
            }
        }
        when (event.phase) {
            AppLogPhase.Failure -> Log.e(TAG, message, event.throwable)
            AppLogPhase.Cancel -> Log.w(TAG, message, event.throwable)
            AppLogPhase.Start, AppLogPhase.Success -> Log.d(TAG, message, event.throwable)
        }
    }

    private companion object {
        const val TAG = "WXXStoreManage"
    }
}
