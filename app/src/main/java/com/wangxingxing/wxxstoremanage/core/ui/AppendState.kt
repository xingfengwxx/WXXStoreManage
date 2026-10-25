/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 分页追加状态，区分空闲、加载、失败与到底。
 */
package com.wangxingxing.wxxstoremanage.core.ui

import com.wangxingxing.wxxstoremanage.core.common.AppErrorType

sealed interface AppendState {
    data object Idle : AppendState
    data object Loading : AppendState
    data class Failed(val type: AppErrorType) : AppendState
    data object End : AppendState
}
