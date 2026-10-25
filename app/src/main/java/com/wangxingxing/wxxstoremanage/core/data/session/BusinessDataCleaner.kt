/**
 * author : 王星星
 * date : 2026-10-25 15:28
 * email : 1099420259@qq.com
 * description : 业务缓存清理契约；与认证仓库分离，避免退出登录流程直接依赖 Room。
 */
package com.wangxingxing.wxxstoremanage.core.data.session

interface BusinessDataCleaner {
    suspend fun clear()
}
