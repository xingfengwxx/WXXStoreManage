/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : AppLogger 的 Logcat 实现；按阶段选择日志级别，超长消息按 UTF-8 字节分片输出以避免截断，禁止写入 token、密码和个人信息。
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
        val priority = when (event.phase) {
            AppLogPhase.Failure -> Log.ERROR
            AppLogPhase.Cancel -> Log.WARN
            AppLogPhase.Start, AppLogPhase.Success -> Log.DEBUG
        }
        writeLog(priority, message)
        event.throwable?.let { writeLog(priority, Log.getStackTraceString(it)) }
    }

    override fun debug(section: String, message: String) {
        writeLog(Log.DEBUG, "[$section] $message")
    }

    // logcat 单条消息上限约 4KB，HTTP BODY 等长日志会被静默截断；分片后带序号输出便于还原完整内容。
    private fun writeLog(priority: Int, message: String) {
        val chunks = message.chunkByUtf8Bytes(MAX_CHUNK_CONTENT_BYTES)
        chunks.forEachIndexed { index, chunk ->
            val prefix = if (chunks.size > 1) "[${index + 1}/${chunks.size}] " else ""
            Log.println(priority, TAG, prefix + chunk)
        }
    }

    private companion object {
        const val TAG = "WXXStoreManage"

        // 预留序号前缀空间，保证“前缀 + 分片”不超过 logcat 单条约 4000 字节的上限。
        const val MAX_CHUNK_CONTENT_BYTES = 3_900
    }
}

// 按 UTF-8 字节数切分，避免中文等多字节字符被截成乱码。
internal fun String.chunkByUtf8Bytes(maxBytes: Int): List<String> {
    if (toByteArray(Charsets.UTF_8).size <= maxBytes) return listOf(this)

    val chunks = mutableListOf<String>()
    val chunk = StringBuilder()
    var chunkBytes = 0
    var index = 0
    while (index < length) {
        val codePoint = codePointAt(index)
        val byteCount = codePoint.utf8ByteCount()
        if (chunkBytes + byteCount > maxBytes) {
            chunks += chunk.toString()
            chunk.setLength(0)
            chunkBytes = 0
        }
        chunk.appendCodePoint(codePoint)
        chunkBytes += byteCount
        index += Character.charCount(codePoint)
    }
    if (chunk.isNotEmpty()) chunks += chunk.toString()
    return chunks
}

private fun Int.utf8ByteCount(): Int = when {
    this < 0x80 -> 1
    this < 0x800 -> 2
    this < 0x10000 -> 3
    else -> 4
}
