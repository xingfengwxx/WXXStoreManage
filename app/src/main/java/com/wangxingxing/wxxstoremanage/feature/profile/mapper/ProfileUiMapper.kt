/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 当前用户到个人页展示模型的映射。
 */
package com.wangxingxing.wxxstoremanage.feature.profile.mapper

import com.wangxingxing.wxxstoremanage.core.model.CurrentUser
import com.wangxingxing.wxxstoremanage.feature.profile.model.ProfileUiModel

fun CurrentUser.toUiModel(): ProfileUiModel = ProfileUiModel(
    displayName = displayName,
    username = username,
    email = email,
    avatarUrl = avatarUrl,
    userId = id,
)
