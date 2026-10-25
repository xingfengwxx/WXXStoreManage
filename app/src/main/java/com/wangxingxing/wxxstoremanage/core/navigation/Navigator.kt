/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 唯一导航入口，提供 navigate、goBack、replaceTop；一级页切换标签，返回时回到起始一级页。
 */
package com.wangxingxing.wxxstoremanage.core.navigation

class Navigator(
    private val state: NavigationState,
) {

    fun navigate(route: AppRoute) {
        if (route in state.topLevelRoutes) {
            state.selectTopLevelRoute(route)
        } else {
            state.currentBackStack.add(route)
        }
    }

    fun goBack() {
        val backStack = state.currentBackStack
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        } else {
            state.selectTopLevelRoute(state.startRoute)
        }
    }

    fun replaceTop(route: AppRoute) {
        if (route in state.topLevelRoutes) {
            state.selectTopLevelRoute(route)
        } else {
            state.currentBackStack.apply {
                removeLastOrNull()
                add(route)
            }
        }
    }
}
