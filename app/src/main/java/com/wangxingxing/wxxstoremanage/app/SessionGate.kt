/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 根级认证边界；登录页不进入 Nav3 返回栈，登出或会话失效时直接销毁主壳。
 */
package com.wangxingxing.wxxstoremanage.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wangxingxing.wxxstoremanage.core.ui.LoadingIndicator
import com.wangxingxing.wxxstoremanage.feature.auth.route.LoginRoute

@Composable
fun SessionGate(
    modifier: Modifier = Modifier,
    viewModel: SessionGateViewModel = hiltViewModel(),
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    when (sessionState) {
        SessionState.Loading -> LoadingIndicator(modifier = modifier.fillMaxSize())
        SessionState.LoggedOut -> LoginRoute(modifier = modifier.fillMaxSize())
        SessionState.LoggedIn -> AppShell(modifier = modifier.fillMaxSize())
    }
}
