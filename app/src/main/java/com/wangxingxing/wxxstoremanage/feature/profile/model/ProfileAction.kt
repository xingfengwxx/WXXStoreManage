/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 个人页交互动作；退出成功后不发送导航事件，由 SessionGate 切回登录页。
 */
package com.wangxingxing.wxxstoremanage.feature.profile.model

sealed interface ProfileAction {
    data object Retry : ProfileAction
    data object RequestLogout : ProfileAction
    data object DismissLogout : ProfileAction
    data object ConfirmLogout : ProfileAction
}
