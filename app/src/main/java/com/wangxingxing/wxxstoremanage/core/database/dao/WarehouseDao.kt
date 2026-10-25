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
