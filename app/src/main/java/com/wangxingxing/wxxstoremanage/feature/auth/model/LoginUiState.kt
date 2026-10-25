/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 登录页状态；密码只保存在这个页面状态中，勾选记住时由仓库加密落盘，不进入日志。
 */
package com.wangxingxing.wxxstoremanage.feature.auth.model

import com.wangxingxing.wxxstoremanage.core.common.AppErrorType
import com.wangxingxing.wxxstoremanage.core.ui.SubmitState

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val rememberCredentials: Boolean = true,
    val fieldErrors: Map<LoginField, AppErrorType> = emptyMap(),
    val submitState: SubmitState = SubmitState.Idle,
)

enum class LoginField {
    Username,
    Password,
}
