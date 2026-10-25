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
