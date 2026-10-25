/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 下拉刷新状态，区分空闲、刷新中与失败。
 */
package com.wangxingxing.wxxstoremanage.core.ui

import com.wangxingxing.wxxstoremanage.core.common.AppErrorType

sealed interface RefreshState {
    data object Idle : RefreshState
    data object Refreshing : RefreshState
    data class Failed(val type: AppErrorType) : RefreshState
}
