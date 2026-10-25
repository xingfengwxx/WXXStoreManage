/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 定义底部四个一级页的路由、标签与图标映射。
 */
package com.wangxingxing.wxxstoremanage.app

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.wangxingxing.wxxstoremanage.R
import com.wangxingxing.wxxstoremanage.core.navigation.AppRoute
import com.wangxingxing.wxxstoremanage.feature.documents.route.Documents as DocumentsDestination
import com.wangxingxing.wxxstoremanage.feature.home.route.Home as HomeDestination
import com.wangxingxing.wxxstoremanage.feature.inventory.route.Inventory as InventoryDestination
import com.wangxingxing.wxxstoremanage.feature.profile.route.Profile as ProfileDestination

internal enum class TopLevelTab(
    val route: AppRoute,
    // 显式限定到构造参数，消除注解目标歧义警告。
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home(
        route = HomeDestination,
        labelRes = R.string.tab_home,
        icon = Icons.Default.Home,
    ),
    Inventory(
        route = InventoryDestination,
        labelRes = R.string.tab_inventory,
        icon = Icons.Default.ShoppingCart,
    ),
    Documents(
        route = DocumentsDestination,
        labelRes = R.string.tab_documents,
        icon = Icons.AutoMirrored.Filled.List,
    ),
    Profile(
        route = ProfileDestination,
        labelRes = R.string.tab_profile,
        icon = Icons.Default.Person,
    ),
}
