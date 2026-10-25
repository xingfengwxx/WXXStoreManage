/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 库存数据访问接口，提供汇总、按仓库观察与批量 upsert；汇总当前为全局口径。
 */
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

    // 汇总为全局口径，不按当前仓库过滤；按仓库查询走 observeByWarehouse。
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
