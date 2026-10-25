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
