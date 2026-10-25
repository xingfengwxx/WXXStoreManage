/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 我的页路由键与 Route；负责收集当前用户状态并转发退出登录动作。
 */
package com.wangxingxing.wxxstoremanage.feature.profile.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wangxingxing.wxxstoremanage.core.navigation.AppRoute
import com.wangxingxing.wxxstoremanage.feature.profile.screen.ProfileScreen
import com.wangxingxing.wxxstoremanage.feature.profile.viewmodel.ProfileViewModel
import kotlinx.serialization.Serializable

@Serializable
data object Profile : AppRoute

@Composable
fun ProfileRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
