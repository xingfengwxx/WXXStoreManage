/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 登录页 Route；登录页不在 Nav3 返回栈内，因此不持有 Navigator。
 */
package com.wangxingxing.wxxstoremanage.feature.auth.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wangxingxing.wxxstoremanage.feature.auth.screen.LoginScreen
import com.wangxingxing.wxxstoremanage.feature.auth.viewmodel.LoginViewModel

@Composable
fun LoginRoute(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
