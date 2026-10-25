/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 结构化日志事件与 AppLogger 接口；字段包含功能、动作、阶段、上下文和耗时。
 */
package com.wangxingxing.wxxstoremanage.core.common

enum class AppLogPhase {
    Start,
    Success,
    Failure,
    Cancel,
}

data class AppLogEvent(
    val feature: String,
    val action: String,
    val phase: AppLogPhase,
    val context: Map<String, String> = emptyMap(),
    val durationMs: Long? = null,
    val throwable: Throwable? = null,
)

interface AppLogger {
    fun log(event: AppLogEvent)
}
