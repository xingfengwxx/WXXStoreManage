package com.wangxingxing.wxxstoremanage.feature.home.model

sealed interface HomeAction {
    data object Retry : HomeAction
}
