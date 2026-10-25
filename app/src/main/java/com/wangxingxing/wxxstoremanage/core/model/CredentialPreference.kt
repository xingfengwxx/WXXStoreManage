/**
 * author : 王星星
 * date : 2026-10-25 17:27
 * email : 1099420259@qq.com
 * description : 登录页记住凭据偏好；rememberEnabled 为用户上次的明确选择，与是否存有具体凭据相互独立。
 */
package com.wangxingxing.wxxstoremanage.core.model

data class CredentialPreference(
    val credentials: RememberedCredentials?,
    val rememberEnabled: Boolean,
)
