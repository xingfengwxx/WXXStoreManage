/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 应用壳：装配导航状态、底部一级页导航与 NavDisplay 路由注册。
 */
package com.wangxingxing.wxxstoremanage.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.wangxingxing.wxxstoremanage.core.navigation.Navigator
import com.wangxingxing.wxxstoremanage.core.navigation.rememberNavigationState
import com.wangxingxing.wxxstoremanage.feature.documents.route.Documents
import com.wangxingxing.wxxstoremanage.feature.documents.route.DocumentsRoute
import com.wangxingxing.wxxstoremanage.feature.home.route.Home
import com.wangxingxing.wxxstoremanage.feature.home.route.HomeRoute
import com.wangxingxing.wxxstoremanage.feature.inventory.route.Inventory
import com.wangxingxing.wxxstoremanage.feature.inventory.route.InventoryRoute
import com.wangxingxing.wxxstoremanage.feature.profile.route.Profile
import com.wangxingxing.wxxstoremanage.feature.profile.route.ProfileRoute

@Composable
fun AppShell(modifier: Modifier = Modifier) {
    val topLevelRoutes = remember { TopLevelTab.entries.map { it.route }.toSet() }
    val navigationState = rememberNavigationState(
        startRoute = Home,
        topLevelRoutes = topLevelRoutes,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val appEntryProvider = remember {
        entryProvider<NavKey> {
            entry<Home> { HomeRoute() }
            entry<Inventory> { InventoryRoute() }
            entry<Documents> { DocumentsRoute() }
            entry<Profile> { ProfileRoute() }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                TopLevelTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = navigationState.topLevelRoute == tab.route,
                        onClick = { navigator.navigate(tab.route) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavDisplay(
            entries = navigationState.toDecoratedEntries(appEntryProvider),
            onBack = navigator::goBack,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}
