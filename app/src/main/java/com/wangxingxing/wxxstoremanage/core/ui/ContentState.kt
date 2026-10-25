package com.wangxingxing.wxxstoremanage.core.ui

import com.wangxingxing.wxxstoremanage.core.common.AppErrorType

sealed interface ContentState<out T> {
    data object Loading : ContentState<Nothing>
    data object Empty : ContentState<Nothing>
    data class Content<T>(val value: T) : ContentState<T>
    data class Error(val type: AppErrorType) : ContentState<Nothing>
}
