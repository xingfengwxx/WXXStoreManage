/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 从 StockDao 读取库存汇总并映射为领域模型，读取在 IO 调度器执行。
 */
package com.wangxingxing.wxxstoremanage.core.data.repository

import com.wangxingxing.wxxstoremanage.core.common.di.IoDispatcher
import com.wangxingxing.wxxstoremanage.core.data.mapper.toDomain
import com.wangxingxing.wxxstoremanage.core.database.dao.StockDao
import com.wangxingxing.wxxstoremanage.core.model.StockSummary
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class StockRepositoryImpl @Inject constructor(
    private val stockDao: StockDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : StockRepository {

    override fun observeSummary(): Flow<StockSummary> =
        stockDao.observeSummary()
            .map { row -> row.toDomain() }
            .flowOn(ioDispatcher)
}
