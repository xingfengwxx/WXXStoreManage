package com.wangxingxing.wxxstoremanage.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.wangxingxing.wxxstoremanage.core.database.entity.SkuEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkuDao {

    @Query("SELECT * FROM skus WHERE isActive = 1 ORDER BY name")
    fun observeActiveSkus(): Flow<List<SkuEntity>>

    @Upsert
    suspend fun upsertAll(skus: List<SkuEntity>)
}
