/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 结束会话的统一入口；登出与未来的会话失效必须共用此路径。
 */
package com.wangxingxing.wxxstoremanage.core.data.session

import com.wangxingxing.wxxstoremanage.core.common.AppResult

interface SessionCleaner {
    suspend fun endSession(): AppResult<Unit>
}
