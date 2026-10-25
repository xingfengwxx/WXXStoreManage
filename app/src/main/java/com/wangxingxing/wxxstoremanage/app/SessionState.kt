/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 根级认证边界状态；Loading 表示尚未读完本地会话，不是已登录或未登录。
 */
package com.wangxingxing.wxxstoremanage.app

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data object LoggedIn : SessionState
}
