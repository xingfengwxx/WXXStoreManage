/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 单据入口路由键与 Route；当前为无数据静态入口页。
 */
package com.wangxingxing.wxxstoremanage.feature.documents.route

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wangxingxing.wxxstoremanage.core.navigation.AppRoute
import com.wangxingxing.wxxstoremanage.feature.documents.screen.DocumentsScreen
import kotlinx.serialization.Serializable

@Serializable
data object Documents : AppRoute

@Composable
fun DocumentsRoute(modifier: Modifier = Modifier) {
    DocumentsScreen(modifier = modifier)
}
