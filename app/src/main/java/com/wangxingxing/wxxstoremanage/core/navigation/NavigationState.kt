/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 管理一级页选中态与各自的返回栈；注意只有一级页路由会写入 topLevelRouteState，读取时可安全转换为 AppRoute。
 */
package com.wangxingxing.wxxstoremanage.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

class NavigationState(
    val startRoute: AppRoute,
    val topLevelRoutes: Set<AppRoute>,
    private val topLevelRouteState: MutableState<NavKey>,
    val backStacks: Map<AppRoute, NavBackStack<NavKey>>,
) {

    val topLevelRoute: AppRoute
        // 该状态只由 selectTopLevelRoute 写入一级页路由，因此这里的转换是安全的。
        get() = topLevelRouteState.value as AppRoute

    val currentBackStack: NavBackStack<NavKey>
        get() = backStacks.getValue(topLevelRoute)

    internal fun selectTopLevelRoute(route: AppRoute) {
        require(route in topLevelRoutes) { "Route $route is not a top-level route" }
        topLevelRouteState.value = route
    }

    @Composable
    fun toDecoratedEntries(
        entryProvider: (NavKey) -> NavEntry<NavKey>,
    ): List<NavEntry<NavKey>> {
        val decoratedEntries = backStacks.mapValues { (_, backStack) ->
            val decorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                rememberViewModelStoreNavEntryDecorator<NavKey>(),
            )
            rememberDecoratedNavEntries(
                backStack = backStack,
                entryDecorators = decorators,
                entryProvider = entryProvider,
            )
        }
        return topLevelRoutesInUse().flatMap { route -> decoratedEntries[route].orEmpty() }
    }

    // 只渲染起始一级页与当前一级页，其余一级栈保留返回状态但不参与组合。
    private fun topLevelRoutesInUse(): List<AppRoute> =
        if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }
}

@Composable
fun rememberNavigationState(
    startRoute: AppRoute,
    topLevelRoutes: Set<AppRoute>,
): NavigationState {
    require(startRoute in topLevelRoutes) { "Start route $startRoute must be a top-level route" }

    val topLevelRouteState = rememberSerializable(
        startRoute,
        topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer()),
    ) {
        mutableStateOf<NavKey>(startRoute)
    }

    val backStacks = topLevelRoutes.associateWith { route -> rememberNavBackStack(route) }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoutes = topLevelRoutes,
            topLevelRouteState = topLevelRouteState,
            backStacks = backStacks,
        )
    }
}
