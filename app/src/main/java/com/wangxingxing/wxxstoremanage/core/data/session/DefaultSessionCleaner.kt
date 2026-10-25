/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 先结束会话再清业务缓存；Mutex 串行化重复退出，避免清理交叉执行。
 */
package com.wangxingxing.wxxstoremanage.core.data.session

import com.wangxingxing.wxxstoremanage.core.common.AppResult
import com.wangxingxing.wxxstoremanage.core.data.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultSessionCleaner @Inject constructor(
    private val authRepository: AuthRepository,
    private val businessDataCleaner: BusinessDataCleaner,
) : SessionCleaner {

    private val mutex = Mutex()

    override suspend fun endSession(): AppResult<Unit> = mutex.withLock {
        when (val logoutResult = authRepository.logout()) {
            is AppResult.Failure -> logoutResult
            is AppResult.Success -> {
                businessDataCleaner.clear()
                AppResult.Success(Unit)
            }
        }
    }
}
