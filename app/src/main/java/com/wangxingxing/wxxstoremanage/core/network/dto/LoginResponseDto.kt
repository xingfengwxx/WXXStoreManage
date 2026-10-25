/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 登录响应中的用户资料；token 与 admin 故意不映射。
 */
package com.wangxingxing.wxxstoremanage.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val id: Int? = null,
    val username: String = "",
    val nickname: String? = null,
    val email: String? = null,
    val icon: String? = null,
)
