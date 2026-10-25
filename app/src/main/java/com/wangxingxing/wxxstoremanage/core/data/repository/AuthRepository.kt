/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 认证数据访问契约；feature 只观察当前用户，不接触 Cookie、DTO 或 Keystore。
 */
package com.wangxingxing.wxxstoremanage.core.data.repository

import com.wangxingxing.wxxstoremanage.core.common.AppResult
import com.wangxingxing.wxxstoremanage.core.model.CurrentUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val session: Flow<CurrentUser?>

    suspend fun login(username: String, password: String): AppResult<CurrentUser>

    suspend fun logout(): AppResult<Unit>
}
