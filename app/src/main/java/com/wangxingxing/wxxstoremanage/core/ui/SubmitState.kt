package com.wangxingxing.wxxstoremanage.core.ui

import com.wangxingxing.wxxstoremanage.core.common.AppErrorType

sealed interface SubmitState {
    data object Idle : SubmitState
    data object Submitting : SubmitState
    data object Success : SubmitState
    data class Error(val type: AppErrorType) : SubmitState
}
