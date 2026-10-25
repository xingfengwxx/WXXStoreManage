/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 库存汇总的只读仓库接口，向页面暴露领域模型 Flow。
 */
package com.wangxingxing.wxxstoremanage.core.data.repository

import com.wangxingxing.wxxstoremanage.core.model.StockSummary
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun observeSummary(): Flow<StockSummary>
}
