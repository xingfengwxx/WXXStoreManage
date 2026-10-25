package com.wangxingxing.wxxstoremanage.core.data.repository

import com.wangxingxing.wxxstoremanage.core.model.Warehouse
import kotlinx.coroutines.flow.Flow

interface WarehouseRepository {
    fun observeActiveWarehouses(): Flow<List<Warehouse>>
}
