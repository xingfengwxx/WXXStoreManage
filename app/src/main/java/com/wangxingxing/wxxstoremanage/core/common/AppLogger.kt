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
