package com.wangxingxing.wxxstoremanage.core.ui

import com.wangxingxing.wxxstoremanage.core.common.AppErrorType

sealed interface AppendState {
    data object Idle : AppendState
    data object Loading : AppendState
    data class Failed(val type: AppErrorType) : AppendState
    data object End : AppendState
}
