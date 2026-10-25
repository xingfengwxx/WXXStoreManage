package com.wangxingxing.wxxstoremanage.core.ui

import com.wangxingxing.wxxstoremanage.core.common.AppErrorType

sealed interface RefreshState {
    data object Idle : RefreshState
    data object Refreshing : RefreshState
    data class Failed(val type: AppErrorType) : RefreshState
}
