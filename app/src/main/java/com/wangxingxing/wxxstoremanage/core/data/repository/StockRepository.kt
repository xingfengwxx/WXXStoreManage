package com.wangxingxing.wxxstoremanage.core.data.repository

import com.wangxingxing.wxxstoremanage.core.model.StockSummary
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    fun observeSummary(): Flow<StockSummary>
}
