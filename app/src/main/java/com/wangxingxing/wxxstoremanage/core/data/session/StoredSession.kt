/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 加密持久化的会话快照；只包含 Cookie 与展示资料，永不写入 token、密码或 admin。
 */
package com.wangxingxing.wxxstoremanage.core.data.session

import kotlinx.serialization.Serializable

@Serializable
data class StoredSession(
    val cookie: String,
    val userId: Long? = null,
    val username: String,
    val displayName: String,
    val email: String? = null,
    val avatarUrl: String? = null,
) {
    init {
        require(cookie.isNotBlank()) { "Session cookie must not be blank" }
    }
}
