/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : 仓库数据访问接口，提供启用仓库列表观察与批量 upsert。
 */
package com.wangxingxing.wxxstoremanage.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wangxingxing.wxxstoremanage.core.database.entity.WarehouseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {

    @Query("SELECT * FROM warehouses WHERE isActive = 1 ORDER BY name")
    fun observeActiveWarehouses(): Flow<List<WarehouseEntity>>

    @Upsert
    suspend fun upsertAll(warehouses: List<WarehouseEntity>)
}
