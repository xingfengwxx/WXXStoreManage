/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 个人页展示模型；邮箱、头像与用户 ID 允许缺失，缺失时不渲染对应内容。
 */
package com.wangxingxing.wxxstoremanage.feature.profile.model

data class ProfileUiModel(
    val displayName: String,
    val username: String,
    val email: String?,
    val avatarUrl: String?,
    val userId: Long?,
)
