package com.wangxingxing.wxxstoremanage.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wangxingxing.wxxstoremanage.core.database.entity.StockEntity
import kotlinx.coroutines.flow.Flow

data class StockSummaryRow(
    val skuCount: Int,
    val onHand: Int,
    val locked: Int,
)

@Dao
interface StockDao {

    @Query(
        """
        SELECT COUNT(*) AS skuCount,
               COALESCE(SUM(onHand), 0) AS onHand,
               COALESCE(SUM(locked), 0) AS locked
        FROM stocks
        """,
    )
    fun observeSummary(): Flow<StockSummaryRow>

    @Query("SELECT * FROM stocks WHERE warehouseId = :warehouseId ORDER BY skuId")
    fun observeByWarehouse(warehouseId: String): Flow<List<StockEntity>>

    @Upsert
    suspend fun upsertAll(stocks: List<StockEntity>)
}
