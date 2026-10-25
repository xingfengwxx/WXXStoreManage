/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 本机已认证用户；不包含角色，玩Android 的 admin 字段不代表仓储业务角色。
 */
package com.wangxingxing.wxxstoremanage.core.model

data class CurrentUser(
    val id: Long?,
    val username: String,
    val displayName: String,
    val email: String?,
    val avatarUrl: String?,
)
