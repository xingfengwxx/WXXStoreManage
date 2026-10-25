/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : HTTP 调试日志脱敏；只替换可重放凭据的值，保留 URL、状态码、报文字段与业务错误码。
 */
package com.wangxingxing.wxxstoremanage.core.network.logging

private val formSecretPattern = Regex("(?i)((?:password|token)=)[^&\\s]+")
private val jsonSecretPattern = Regex("(?i)(\"(?:password|token)\"\\s*:\\s*\")[^\"]*(\")")
private val credentialHeaderPattern = Regex("(?i)((?:cookie|set-cookie|authorization)\\s*:\\s*).*")
private val sessionCookiePattern = Regex("(?i)(JSESSIONID=)[^;\\s&]+")

internal fun redactSensitiveLog(message: String): String = message
    .replace(formSecretPattern, "$1***")
    .replace(jsonSecretPattern, "$1***$2")
    .replace(credentialHeaderPattern, "$1***")
    .replace(sessionCookiePattern, "$1***")
