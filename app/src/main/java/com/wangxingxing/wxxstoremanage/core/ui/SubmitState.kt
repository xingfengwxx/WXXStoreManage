/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 写操作提交状态，区分空闲、提交中、成功与失败。
 */
package com.wangxingxing.wxxstoremanage.core.ui

import com.wangxingxing.wxxstoremanage.core.common.AppErrorType

sealed interface SubmitState {
    data object Idle : SubmitState
    data object Submitting : SubmitState
    data object Success : SubmitState
    data class Error(val type: AppErrorType) : SubmitState
}
