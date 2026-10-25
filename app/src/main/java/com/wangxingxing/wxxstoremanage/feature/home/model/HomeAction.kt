/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 首页用户动作契约，当前仅包含重试。
 */
package com.wangxingxing.wxxstoremanage.feature.home.model

sealed interface HomeAction {
    data object Retry : HomeAction
}
