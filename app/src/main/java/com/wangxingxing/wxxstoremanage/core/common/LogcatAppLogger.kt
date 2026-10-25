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
