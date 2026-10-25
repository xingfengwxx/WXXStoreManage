/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 登录页交互动作；登录成功不产生动作，页面切换由会话状态驱动。
 */
package com.wangxingxing.wxxstoremanage.feature.auth.model

sealed interface LoginAction {
    data class UsernameChanged(val value: String) : LoginAction
    data class PasswordChanged(val value: String) : LoginAction
    data object PasswordVisibilityToggled : LoginAction
    data object Submit : LoginAction
}
