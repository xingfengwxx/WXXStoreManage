/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 我的页路由键与 Route；当前为无数据静态入口页。
 */
package com.wangxingxing.wxxstoremanage.feature.profile.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wangxingxing.wxxstoremanage.core.navigation.AppRoute
import com.wangxingxing.wxxstoremanage.feature.profile.screen.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
data object Profile : AppRoute

@Composable
fun ProfileRoute(modifier: Modifier = Modifier) {
    ProfileScreen(modifier = modifier)
}
