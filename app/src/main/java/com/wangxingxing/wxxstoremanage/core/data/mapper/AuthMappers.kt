/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 登录 DTO 与会话快照到领域用户的映射；空资料降级，缺失用户名视为无效响应。
 */
package com.wangxingxing.wxxstoremanage.core.data.mapper

import com.wangxingxing.wxxstoremanage.core.data.session.StoredSession
import com.wangxingxing.wxxstoremanage.core.model.CurrentUser
import com.wangxingxing.wxxstoremanage.core.network.dto.LoginResponseDto

fun LoginResponseDto.toCurrentUser(): CurrentUser? {
    val validUsername = username.takeIf { it.isNotBlank() } ?: return null
    return CurrentUser(
        id = id?.toLong(),
        username = validUsername,
        displayName = nickname?.takeIf { it.isNotBlank() } ?: validUsername,
        email = email?.takeIf { it.isNotBlank() },
        avatarUrl = icon?.takeIf { it.isNotBlank() },
    )
}

fun StoredSession.toCurrentUser(): CurrentUser = CurrentUser(
    id = userId,
    username = username,
    displayName = displayName,
    email = email,
    avatarUrl = avatarUrl,
)

fun CurrentUser.toStoredSession(cookie: String): StoredSession = StoredSession(
    cookie = cookie,
    userId = id,
    username = username,
    displayName = displayName,
    email = email,
    avatarUrl = avatarUrl,
)
