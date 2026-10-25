/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 退出登录时清空 Room 业务数据；清理失败只记录，不撤销已完成的会话清除。
 */
package com.wangxingxing.wxxstoremanage.core.data.session

import com.wangxingxing.wxxstoremanage.core.common.AppLogEvent
import com.wangxingxing.wxxstoremanage.core.common.AppLogPhase
import com.wangxingxing.wxxstoremanage.core.common.AppLogger
import com.wangxingxing.wxxstoremanage.core.common.di.IoDispatcher
import com.wangxingxing.wxxstoremanage.core.database.AppDatabase
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RoomBusinessDataCleaner @Inject constructor(
    private val database: AppDatabase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val logger: AppLogger,
) : BusinessDataCleaner {

    override suspend fun clear() {
        try {
            withContext(ioDispatcher) {
                database.clearAllTables()
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            logger.log(
                AppLogEvent(
                    feature = FEATURE,
                    action = ACTION_CLEAR_BUSINESS_DATA,
                    phase = AppLogPhase.Failure,
                    throwable = throwable,
                ),
            )
        }
    }

    private companion object {
        const val FEATURE = "session"
        const val ACTION_CLEAR_BUSINESS_DATA = "clearBusinessData"
    }
}
