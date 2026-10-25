/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 个人页状态；退出登录使用独立 SubmitState，确认弹窗由可持续观察的布尔状态承载。
 */
package com.wangxingxing.wxxstoremanage.feature.profile.model

import com.wangxingxing.wxxstoremanage.core.ui.ContentState
import com.wangxingxing.wxxstoremanage.core.ui.SubmitState

data class ProfileUiState(
    val content: ContentState<ProfileUiModel> = ContentState.Loading,
    val logoutState: SubmitState = SubmitState.Idle,
    val showLogoutConfirmation: Boolean = false,
)
