/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 首屏内容四态，统一 Loading、Empty、Content 与 Error。
 */
package com.wangxingxing.wxxstoremanage.core.ui

import com.wangxingxing.wxxstoremanage.core.common.AppErrorType

sealed interface ContentState<out T> {
    data object Loading : ContentState<Nothing>
    data object Empty : ContentState<Nothing>
    data class Content<T>(val value: T) : ContentState<T>
    data class Error(val type: AppErrorType) : ContentState<Nothing>
}
